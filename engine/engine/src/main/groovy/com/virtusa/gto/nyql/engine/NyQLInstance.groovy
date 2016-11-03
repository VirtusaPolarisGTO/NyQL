package com.virtusa.gto.nyql.engine

import com.virtusa.gto.nyql.DSLContext
import com.virtusa.gto.nyql.configs.ConfigBuilder
import com.virtusa.gto.nyql.configs.ConfigKeys
import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.engine.impl.QExternalJdbcFactory
import com.virtusa.gto.nyql.engine.impl.QJdbcExecutor
import com.virtusa.gto.nyql.engine.repo.QSingleScript
import com.virtusa.gto.nyql.engine.repo.QSingleScriptRepository
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.QRepository
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QSession
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import java.sql.Connection
/**
 * @author IWEERARATHNA
 */
@CompileStatic
class NyQLInstance {
    
    private static final Map<String, Object> EMPTY_MAP = [:]

    private final Configurations configurations

    private NyQLInstance(Configurations theConfigInstance) {
        this.configurations = theConfigInstance
    }

    static NyQLInstance create(File configFile) {
        Map configData = new JsonSlurper().parse(configFile) as Map
        configData.put(ConfigKeys.LOCATION_KEY, new File('.').canonicalPath)
        Configurations configInst = ConfigBuilder.instance().setupFrom(configData).build()
        create(configInst)
    }

    static NyQLInstance create(Configurations configInst) {
        new NyQLInstance(configInst)
    }

    /**
     * <p>
     * Parse the given file indicated from given script name and returns the generated query
     * with its other information. Your script name would be the relative path from the
     * script root directory, always having forward slashes (/).
     * </p>
     * You should call this <b>only</b> if you are working with a script repository.
     *
     * @param scriptName name of the script.
     * @return generated query instance.
     * @throws com.virtusa.gto.nyql.exceptions.NyException any exception thrown while parsing.
     */
    @CompileStatic
    QScript parse(String scriptName) throws NyException {
        parse(scriptName, EMPTY_MAP)
    }

    /**
     * <p>
     * Parse the given file indicated from given script name using the given variable set
     * and returns the generated query
     * with its other information. Your script name would be the relative path from the
     * script root directory, always having forward slashes (/).
     * </p>
     * You should call this <b>only</b> if you are working with a script repository.
     *
     * @param scriptName name of the script.
     * @param data set of variable data required for generation of query.
     * @return generated query instance.
     * @throws NyException any exception thrown while parsing.
     */
    @CompileStatic
    QScript parse(String scriptName, Map<String, Object> data) throws NyException {
        QSession qSession = QSession.create(configurations, scriptName)
        if (data) {
            qSession.sessionVariables.putAll(data)
        }
        configurations.repositoryRegistry.defaultRepository().parse(scriptName, qSession)
    }

    /**
     * Shutdown the nyql engine.
     * This should be called only when your application exits.
     */
    void shutdown() {
        Configurations.instance().shutdown()
    }

    /**
     * <p>
     * Executes a given file indicated by the script name and returns the final result
     * which was output of the last statement in the script ran.
     * </p>
     * <p>
     * This method will automatically parse the script and execute using internally
     * configured executor.
     * </p>
     *
     * @param scriptName name of the script to be run.
     * @return the result of the script execution.
     * @throws NyException any exception thrown while parsing or executing.
     */
    @CompileStatic
    <T> T execute(String scriptName) throws NyException {
        (T) execute(scriptName, EMPTY_MAP)
    }

    /**
     * <p>
     * Executes a given file indicated by the script name using given set of variables
     * and returns the final result
     * which was output of the last statement in the script ran.
     * </p>
     * <p>
     * This method will automatically parse the script and execute using internally
     * configured executor.
     * </p>
     * <b>Note:</b><br/>
     * You should pass all parameter values required for the query execution.
     *
     * @param scriptName name of the script to be run.
     * @param data set of variables to be passed to the script run.
     * @return the result of the script execution.
     * @throws NyException any exception thrown while parsing or executing.
     */
    @CompileStatic
    <T> T execute(String scriptName, Map<String, Object> data) throws NyException {
        QScript script = null
        try {
            script = parse(scriptName, data)
            (T) configurations.executorRegistry.defaultExecutorFactory().create().execute(script)
        } finally {
            if (script != null) {
                script.free()
            }
        }
    }

    /**
     * <p>
     * Executes a given file indicated by the script name and returns the final result
     * which was output of the last statement in the script ran.
     * </p>
     * <p>
     * This method will automatically parse the script and execute using internally
     * configured executor.
     * </p>
     *
     * @param scriptName name of the script to be run.
     * @return the result of the script execution.
     * @throws NyException any exception thrown while parsing or executing.
     */
    @CompileStatic
    <T> T execute(String dslSql, Map<String, Object> data, Connection connection) throws NyException {
        String scriptId = String.valueOf(System.currentTimeMillis())
        execute(scriptId, dslSql, data, connection)
    }

    /**
     * <p>
     * Executes a script by providing its content and a jdbc connection at the runtime.
     * The given connection will be used to execute the script. </p>
     * <b>WARNING:</b>
     * There is no guarantee that it will cache your given script if you invoked this
     * with multiple times with the same content, since it is all about caching in <code>GroovyShell</code>.
     * NyQL <b>does not do</b> any caching at this level and unable to do so.
     *
     * @param scriptId a unique script id for.
     * @param dslSql dsl script content.
     * @param data set of variables required for the script.
     * @param connection jdbc connection to execute after parsing the script.
     * @return the result of the script execution.
     * @throws NyException any exception thrown while executing or parsing.
     */
    @CompileStatic
    <T> T execute(String scriptId, String dslSql, Map<String, Object> data, Connection connection) throws NyException {
        QJdbcExecutor jdbcExecutor = new QJdbcExecutor(connection)
        QSingleScript qSingleScript = new QSingleScript(scriptId, dslSql)
        QRepository repository = new QSingleScriptRepository(qSingleScript)
        QSession session = QSession.createSession(DSLContext.getActiveDSLContext().activeFactory,
                repository, jdbcExecutor, new QExternalJdbcFactory(connection))

        session.sessionVariables.putAll(data ?: [:])
        QScript script = repository.parse(scriptId, session)
        (T) jdbcExecutor.execute(script)
    }

    /**
     * Executes the given script and returns the result as a json string.
     * <p>
     *     If you still want to parse the json again, use the other execute method
     *     <code>execute(String, Map)</code>.
     * </p>
     *
     * @param scriptName name of the script to run.
     * @param data set of variables required for script.
     * @return result as json string.
     * @throws NyException any exception thrown while executing and parsing.
     */
    @CompileStatic
    String executeToJSON(String scriptName, Map<String, Object> data) throws NyException {
        Object result = execute(scriptName, data)
        JsonOutput.toJson(result)
    }

    /**
     * Executes the given script and returns the result as a json string.
     * <p>
     *     If you still want to parse the json again, use the other execute method
     *     <code>execute(String, Map)</code>.
     * </p>
     *
     * @param scriptName name of the script to run.
     * @return result as json string.
     * @throws NyException any exception thrown while executing and parsing.
     */
    @CompileStatic
    String executeToJSON(String scriptName) throws NyException {
        executeToJSON(scriptName, [:])
    }
}
