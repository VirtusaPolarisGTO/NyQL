package com.virtusa.gto.nyql.engine.impl

import com.virtusa.gto.nyql.UpsertQuery
import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.engine.exceptions.NyParamNotFoundException
import com.virtusa.gto.nyql.engine.exceptions.NyScriptExecutionException
import com.virtusa.gto.nyql.engine.pool.QJdbcPoolFetcher
import com.virtusa.gto.nyql.engine.transform.JdbcCallResultTransformer
import com.virtusa.gto.nyql.engine.transform.JdbcCallTransformInput
import com.virtusa.gto.nyql.engine.transform.JdbcResultTransformer
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.*
import com.virtusa.gto.nyql.model.units.*
import com.virtusa.gto.nyql.utils.QReturnType
import com.virtusa.gto.nyql.utils.QUtils
import com.virtusa.gto.nyql.utils.QueryType
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@java.lang.SuppressWarnings('JdbcStatementReference')
import java.sql.CallableStatement
@java.lang.SuppressWarnings('JdbcConnectionReference')
import java.sql.Connection
import java.sql.PreparedStatement
@java.lang.SuppressWarnings('JdbcResultSetReference')
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Savepoint
import java.sql.Statement
import java.util.stream.Collectors

/**
 * @author IWEERARATHNA
 */
class QJdbcExecutor implements QExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(QJdbcExecutor)

    private static final JdbcResultTransformer transformer = new JdbcResultTransformer()
    private static final JdbcCallResultTransformer callResultTransformer = new JdbcCallResultTransformer()

    private final QJdbcPoolFetcher poolFetcher
    private Connection connection
    private final boolean returnRaw
    private boolean reusable
    private Configurations nyqlConfigs
    private int logLevel = 1

    /**
     * Creates an executor with custom connection.
     * In here we won't close the connection at the end of execution.
     *
     * @param yourConnection sql connection
     */
    QJdbcExecutor(Connection yourConnection, Configurations configurations) {
        poolFetcher = null
        connection = yourConnection
        reusable = true
        returnRaw = false
        nyqlConfigs = configurations
        logLevel = configurations.getQueryLoggingLevel()
    }

    QJdbcExecutor(QJdbcPoolFetcher jdbcPoolFetcher, Configurations configurations) {
        this(jdbcPoolFetcher, false, configurations)
    }

    QJdbcExecutor(QJdbcPoolFetcher jdbcPoolFetcher, boolean canReusable, Configurations configurations) {
        poolFetcher = jdbcPoolFetcher
        reusable = canReusable
        returnRaw = false
        nyqlConfigs = configurations
        logLevel = configurations.getQueryLoggingLevel()
    }

    @CompileStatic
    private Connection getConnection() {
        if (connection == null && poolFetcher != null) {
            connection = poolFetcher.getConnection()
        }
        return connection
    }

    @CompileStatic
    @Override
    def execute(QScript script) throws Exception {
        if (script instanceof QScriptResult) {
            return ((QScriptResult)script).scriptResult
        }

        PreparedStatement statement = null
        try {
            JdbcHelperUtils.logScript(script, logLevel)

            if (script.proxy != null && script.proxy.queryType == QueryType.DB_FUNCTION) {
                return executeCall(script)
            }
            if (script.proxy.queryType == QueryType.BULK_INSERT || script.proxy.queryType == QueryType.BULK_UPDATE) {
                return batchExecute(script)
            }

            Map<String, Object> data = script.qSession.sessionVariables
            List<AParam> parameters = script.proxy.orderedParameters
            statement = prepareStatement(script, parameters, data)

            if (script.proxy.queryType == QueryType.SELECT) {
                if (returnRaw) {
                    LOGGER.debug('Returning raw result')
                    return statement.executeQuery()
                } else {
                    //LOGGER.trace('Transforming result set using {}', transformer.class.name)
                    return transformer.apply(statement.executeQuery())
                }
            } else {
                int count = statement.executeUpdate()
                List keys = [] as LinkedList
                if (count > 0 && isReturnKeys(script)) {
                    ResultSet genKeys
                    try {
                        genKeys = statement.getGeneratedKeys()
                        while (genKeys.next()) {
                            Object val = genKeys.getObject(1)
                            keys.add(val)
                        }
                    } finally {
                        if (genKeys != null) {
                            genKeys.close()
                        }
                    }
                }
                return toMap(count, keys)

            }

        } catch (SQLException ex) {
            throw new NyScriptExecutionException(ex.getMessage(), ex)

        } finally {
            if (statement != null) {
                statement.close()
            }
            closeConnection()
        }
    }

    /**
     * Executes the script as a batch and returns number of updated/inserted count as
     * the result set.
     *
     * @param script script to be executed.
     * @return total number of updated rows.
     * @throws Exception any exception thrown while executing batch.
     */
    private def batchExecute(QScript script) throws Exception {
        LOGGER.debug('Executing as batch...')
        PreparedStatement statement = null
        boolean prevCommitStatus = true
        try {
            statement = getConnection().prepareStatement(script.proxy.query)
            prevCommitStatus = connection.getAutoCommit()
            connection.setAutoCommit(false)

            List<AParam> parameters = script.proxy.orderedParameters
            Map sVariables = script.qSession.sessionVariables
            Object batchData = sVariables[JDBCConstants.BATCH_ALT_KEY]
            if (batchData == null) {
                LOGGER.warn('[DEPRECATED] Use the key "__batch__" to provide data for all batch operations ' +
                        'instead of "batch".')
                batchData = sVariables[JDBCConstants.BATCH_KEY]
            }

            if (batchData == null) {
                throw new NyScriptExecutionException("No batch data has been specified through session variables 'batch'!")
            } else if (!(batchData instanceof List)) {
                throw new NyScriptExecutionException('Batch data expected to be a list of hashmaps!')
            }

            List<Map> records = batchData as List<Map>
            for (Map record : records) {
                assignParameters(statement, parameters, record, sVariables)
                statement.addBatch()
            }

            int[] counts = statement.executeBatch()
            connection.commit()
            return new NyQLResult().appendCounts(counts)

        } finally {
            if (prevCommitStatus) {
                connection.setAutoCommit(true)
            }

            if (statement != null) {
                statement.close()
            }
            closeConnection()
        }
    }

    /**
     * Call a stored function in the database and returns the result.
     *
     * @param script script to be executed.
     * @return result of the stored function.
     * @throws Exception any exception thrown while executing db function.
     */
    private def executeCall(QScript script) throws Exception {
        CallableStatement statement = null
        try {
            LOGGER.info("Executing stored function '{}'", script.proxy.query)
            statement = getConnection().prepareCall(script.proxy.query)

            Map<String, Object> data = script.qSession.sessionVariables ?: [:]
            List<AParam> parameters = script.proxy.orderedParameters ?: []

            // register out parameters
            for (int i = 0; i < parameters.size(); i++) {
                AParam param = parameters[i]
                if (!(param instanceof NamedParam)) {
                    throw new NyScriptExecutionException('Stored functions required to have named parameters!')
                }
                NamedParam namedParam = param as NamedParam
                if (namedParam.scope == null || namedParam.scope == AParam.ParamScope.IN) {
                    continue
                }

                LOGGER.trace('  <- Registering output: {}   [Type: {}]', namedParam.__mappingParamName, namedParam.type)
                statement.registerOutParameter(namedParam.__mappingParamName, namedParam.type)
            }

            // set parameter values
            for (int i = 0; i < parameters.size(); i++) {
                NamedParam param = parameters[i] as NamedParam
                Object itemValue = deriveValue(data, param.__name)
                if (param.__mappingParamName == null) {
                    throw new NyException("Mapping parameter name has not been defined for SP input parameter '$param.__name'!")
                }

                JdbcHelperUtils.logParameter(param.__mappingParamName, itemValue, logLevel)
                statement.setObject(param.__mappingParamName, itemValue)
            }

            boolean hasResults = statement.execute()
            if (hasResults) {
                if (returnRaw) {
                    return statement.getResultSet()
                } else {
                    JdbcCallTransformInput input = new JdbcCallTransformInput(statement: statement, script: script)
                    def result = callResultTransformer.apply(input)
                    input.clear()
                    return result
                }
            }

        } finally {
            if (statement != null) {
                statement.close()
            }
            closeConnection()
        }
    }

    /**
     * Closes the connection if reusable is not specified.
     */
    @CompileStatic
    private void closeConnection() {
        if (connection == null || reusable) {
            return
        }
        connection.close()
    }

    @CompileStatic
    private static void assignParameters(PreparedStatement statement, List<AParam> parameters, Map data, Map session) {
        int cp = 1
        for (int i = 0; i < parameters.size(); i++) {
            AParam param = parameters[i]
            Object itemValue = deriveValue(data, param.__name, false)
            if (itemValue == NoneParameter.INSTANCE) {
                // No parameter exist by given name in record map
                // Let's find it in the session map...
                itemValue = deriveValue(session, param.__name)
            }

            statement.setObject(cp++, itemValue)
        }
    }

    private PreparedStatement prepareStatement(QScript script, List<AParam> paramList, Map data) {
        List orderedValues = [] as LinkedList
        String query = script.proxy.query
        int cp = 1

        for (AParam param : paramList) {
            Object itemValue = deriveValue(data, param.__name)

            JdbcHelperUtils.logParameter(cp, itemValue, logLevel)
            if (param instanceof ParamList) {
                if (itemValue instanceof List) {
                    List itemList = (List)itemValue
                    itemList.each { orderedValues.add(it) }
                    String pStr = itemList.stream().map { return '?' }.collect(Collectors.joining(', '))
                    if (itemList.isEmpty()) {
                        LOGGER.warn('Empty parameter list received!')
                        pStr = 'NULL'
                    }
                    query = query.replaceAll(QUtils.padParamList(param.__name), pStr)
                    cp += itemList.size()

                } else {
                    throw new NyScriptExecutionException("Parameter value of '$param.__name' expected to be a list but given " + itemValue.class.simpleName + '!')
                }
            } else {
                if (itemValue instanceof List) {
                    LOGGER.warn('INCOMPATIBLE PARAMETER VALUE DETECTED! ' +
                            'Expected a single value but received a list value for parameter ' + param.__name + '!')
                }

                orderedValues.add(convertValue(itemValue, param, script))
                cp++
            }
        }

        PreparedStatement statement
        if (isReturnKeys(script)) {
            statement = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        } else {
            statement = getConnection().prepareStatement(query)
        }
        cp = 1
        for (Object pValue : orderedValues) {
            statement.setObject(cp++, pValue)
        }
        statement
    }

    @CompileStatic
    private static Object convertValue(Object value, AParam param, QScript qScript) {
        if (param.__shouldValueConvert()) {
            if (value == null) {
                return null
            }

            if (param instanceof ParamTimestamp) {
                JdbcHelperUtils.convertTimestamp(value, qScript.qSession.configurations, ((ParamTimestamp)param).__tsFormat)
            } else if (param instanceof ParamDate) {
                JdbcHelperUtils.convertToDate(String.valueOf(value))
            } else if (param instanceof ParamBinary) {
                JdbcHelperUtils.convertBinary(value)
            } else {
                throw new NyScriptExecutionException('Unknown parameter type specified in script!')
            }
        } else {
            value
        }
    }

    @CompileStatic
    private static boolean isReturnKeys(QScript script) {
        script.proxy != null && script.proxy.queryType == QueryType.INSERT &&
                script.proxy.returnType == QReturnType.KEYS
    }

    private static Object deriveValue(Map dataMap, String name, boolean throwEx = true) {
        if (name.indexOf('.') > 0) {
            String[] parts = name.split('[.]')
            Object res = dataMap
            for (String p : parts) {
                res = res."$p"
            }
            if (res == dataMap) {
                throw new NyScriptExecutionException("The dot-notated parameter '$name' creates a circular reference in session!")
            }
            return res

        } else {
            if (dataMap.containsKey(name)) {
                return dataMap[name]
            } else {
                if (throwEx) {
                    throw new NyParamNotFoundException(name)
                } else {
                    return NoneParameter.INSTANCE
                }
            }
        }
    }

    @CompileStatic
    @Override
    def execute(QScriptList scriptList) throws Exception {
        if (scriptList == null || scriptList.scripts == null) {
            return new LinkedList<>()
        }

        final boolean prevReusable = reusable
        reusable = true

        try {
            if (scriptList.type == QScriptListType.UPSERT) {
                return handleUpsertExecution(scriptList)
            } else if (scriptList.type == QScriptListType.INSERT_OR_LOAD) {
                return handleInsertOrExecution(scriptList)
            } else {
                List results = []
                for (QScript qScript : scriptList.scripts) {
                    def res = execute(qScript)
                    results.add(res)
                }
                return results
            }

        } finally {
            reusable = prevReusable
            closeConnection()
        }
    }

    @CompileStatic
    private handleInsertOrExecution(QScriptList scriptList) throws Exception {
        if (scriptList.scripts.size() < 2) {
            throw new NyException('InsertOrLoad query has missing clauses!')
        }

        NyQLResult result = execute(scriptList.scripts[0]) as NyQLResult
        if (result.isEmpty()) {
            // insert
            execute(scriptList.scripts[1])
            result = execute(scriptList.scripts[0]) as NyQLResult // get the latest value
        }

        return result
    }

    @CompileStatic
    private handleUpsertExecution(QScriptList scriptList) throws Exception {
        if (scriptList.scripts.size() < 3) {
            throw new NyException('Not defined either select, insert, or update query in UPSERT query!')
        }

        UpsertQuery upsertQuery = (UpsertQuery)scriptList.baseQuery

        NyQLResult result = execute(scriptList.scripts[0]) as NyQLResult
        if (result.isEmpty()) {
            // insert
            execute(scriptList.scripts[1])
        } else {
            // records exist... update
            execute(scriptList.scripts[2])
        }

        if (upsertQuery.returningType == UpsertQuery.ReturnType.NONE) {
            return new NyQLResult()
        } else if (upsertQuery.returningType == UpsertQuery.ReturnType.RECORD_BEFORE) {
            return result
        } else {
            if (scriptList.scripts.size() < 4) {
                throw new NyException('Query correspond to returning upsert result is not found!')
            } else {
                return execute(scriptList.scripts[3])
            }
        }
    }

    @Override
    void startTransaction() throws NyException {
        getConnection().setAutoCommit(false)
        LOGGER.info('Starting new transaction.')
    }

    @Override
    void commit() throws NyException {
        connection.commit()
    }

    @Override
    def checkPoint() throws NyException {
        connection.setSavepoint()
    }

    @Override
    void rollback(def checkpoint) throws NyException {
        if (checkpoint != null && checkpoint instanceof Savepoint) {
            connection.rollback(checkpoint)
        } else {
            connection.rollback()
        }
    }

    @Override
    void done() throws NyException {
        connection.setAutoCommit(true)
        LOGGER.info('Transaction completed.')
    }

    private static List<Map> toMap(int count, List keys = null) {
        new NyQLResult().appendCount(count, keys)
    }

    @CompileStatic
    @Override
    void close() throws IOException {
        nyqlConfigs = null
        if (connection != null) {
            connection.close()
        }
    }

    @CompileStatic
    private static class NoneParameter {
        private static final NoneParameter INSTANCE = new NoneParameter()

        private NoneParameter() {}
    }
}
