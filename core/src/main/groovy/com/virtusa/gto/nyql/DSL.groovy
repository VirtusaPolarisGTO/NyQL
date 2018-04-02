package com.virtusa.gto.nyql

import com.virtusa.gto.nyql.ddl.DDL
import com.virtusa.gto.nyql.exceptions.NyScriptNotFoundException
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.exceptions.NySyntaxException
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QScriptList
import com.virtusa.gto.nyql.model.QSession
import com.virtusa.gto.nyql.model.ValueTable
import com.virtusa.gto.nyql.model.units.AParam
import com.virtusa.gto.nyql.model.units.ParamList
import com.virtusa.gto.nyql.utils.QUtils
import com.virtusa.gto.nyql.utils.QueryCombineType
import com.virtusa.gto.nyql.utils.QueryType
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 * @author Isuru Weerarathna
 */
@CompileStatic
class DSL {

    private static final Logger LOGGER = LoggerFactory.getLogger(DSL)

    final QSession session
    //final DSLContext dslContext

    private boolean currentTransactionAutoCommit = false

    public DSL(QSession theSession) {
        session = theSession
        set$SESSION(session.sessionVariables)
        set$DB(session.dbFactory.dbName())
    }

    ///////////////////////////////////////////////////////////////////////////////////
    ////
    ////   Script Execution Related Commands
    ////
    ///////////////////////////////////////////////////////////////////////////////////

    def script(@DelegatesTo(DSL) Closure closure) {
        try {
            session.beingScript()

            def code = closure.rehydrate(this, this, this)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            return code()

        } finally {
            session.closeScript()
        }
    }

    QScript $IMPORT(String scriptName) {
        session.intoScript(scriptName)
        QScript res = session.scriptRepo.parse(scriptName, session)
        session.outFromScript(scriptName)
        res
    }

    QScript $IMPORT_SAFE(String scriptName) {
        try {
            $IMPORT(scriptName)
        } catch (NyScriptNotFoundException ex) {
            return null
        }
    }

    def RUN(QScriptList scriptList) {
        session.execute((QScriptList)scriptList)
    }

    def RUN(QScript script) {
        if (script instanceof QScriptList) {
            RUN((QScriptList)script)
        } else {
            session.execute(script)
        }
    }

    def RUN(QResultProxy proxy) {
        RUN(session.scriptRepo.parse(proxy, session))
    }

    def RUN(String scriptName) {
        RUN($IMPORT(scriptName))
    }

    ///////////////////////////////////////////////////////////////////////////////////
    ////
    ////   Query Related Commands
    ////
    ///////////////////////////////////////////////////////////////////////////////////

    QResultProxy truncate(String tableName) {
        QueryTruncate queryTruncate = new QueryTruncate(createContext())
        Table table = new Table(__name: tableName)
        queryTruncate.sourceTbl = table

        session.dbFactory.createTranslator().___truncateQuery(queryTruncate)
    }

    QResultProxy union(Object... qResultProxies) {
        List list = []
        for (def q : qResultProxies) {
            if (q instanceof QResultProxy) {
                list.add((QResultProxy) q)
            } else if (q instanceof QScript) {
                list.add(((QScript)q).proxy)
            }
        }
        session.dbFactory.createTranslator().___combinationQuery(QueryCombineType.UNION, list)
    }

    QResultProxy unionDistinct(Object... qResultProxies) {
        List list = []
        for (def q : qResultProxies) {
            if (q instanceof QResultProxy) {
                list.add((QResultProxy) q)
            } else if (q instanceof QScript) {
                list.add(((QScript)q).proxy)
            }
        }
        session.dbFactory.createTranslator().___combinationQuery(QueryCombineType.UNION_DISTINCT, list)
    }

    QResultProxy dbFunction(String name, List<AParam> paramList) {
        StoredFunction sp = new StoredFunction(name: name, paramList: new ArrayList<AParam>(paramList))
        session.dbFactory.createTranslator().___storedFunction(sp)
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    QResultProxy nativeQuery(QueryType queryType, List<AParam> orderedParams = [], String query) {
        new QResultProxy(query: String.valueOf(query).trim(),
                queryType: queryType,
                orderedParameters: orderedParams)
    }

    QResultProxy nativeQuery(QueryType queryType, Map queries) {
        String activeDb = session.dbFactory.dbName()
        def query = queries[activeDb]
        if (query == null) {
            throw new NySyntaxException("No query is defined for the active database '${activeDb}'!")
        }
        if (!(query instanceof List)) {
            throw new NySyntaxException('A database specific query must be defined along with its parameter order! Refer NyQL syntax documentation for more info.')
        }
        List qList = (List)query;
        if (qList.size() != 2) {
            throw new NySyntaxException('A native query definition should have exactly 2 items, 1st being the ordered list of parameters and 2nd being the query string!')
        }
        new QResultProxy(query: String.valueOf(qList.get(1)).trim(),
                queryType: queryType,
                orderedParameters: (List)qList.get(0))
    }

    QResultProxy bulkInsert(@DelegatesTo(value = QueryInsert, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        QueryInsert qs = new QueryInsert(createContext())

        def code = closure.rehydrate(qs, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        QResultProxy proxy = qs._ctx.translator.___insertQuery(qs)
        proxy.setQueryType(QueryType.BULK_INSERT)
        proxy
    }

    QResultProxy bulkUpdate(@DelegatesTo(value = QueryUpdate, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        QueryUpdate qs = new QueryUpdate(createContext())

        def code = closure.rehydrate(qs, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        QResultProxy proxy = qs._ctx.translator.___updateQuery(qs)
        proxy.setQueryType(QueryType.BULK_UPDATE)
        proxy
    }

    QResultProxy bulkDelete(@DelegatesTo(value = QueryDelete, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        QueryDelete qd = new QueryDelete(createContext())

        def code = closure.rehydrate(qd, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        QResultProxy proxy = qd._ctx.translator.___deleteQuery(qd)
        proxy.setQueryType(QueryType.BULK_DELETE)
        proxy
    }

    QResultProxy delete(@DelegatesTo(value = QueryDelete, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        QueryDelete qs = new QueryDelete(createContext())
        //Object qs = assignTraits(queryDelete)

        def code = closure.rehydrate(qs, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        qs._ctx.translator.___deleteQuery(qs)
    }

    QResultProxy insert(@DelegatesTo(value = QuerySelect, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        QueryInsert qs = new QueryInsert(createContext())
        //Object qs = assignTraits(queryInsert)

        def code = closure.rehydrate(qs, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        qs._ctx.translator.___insertQuery(qs)
    }

    QResultProxy select(@DelegatesTo(value = QuerySelect, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        QuerySelect qs = new QuerySelect(createContext())
        //Object qs = assignTraits(querySelect)

        def code = closure.rehydrate(qs, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        qs._ctx.translator.___selectQuery(qs)
    }

    QResultProxy update(@DelegatesTo(value = QueryUpdate, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        QueryUpdate qs = new QueryUpdate(createContext())
        //Object qs = assignTraits(queryUpdate)

        def code = closure.rehydrate(qs, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        qs._ctx.translator.___updateQuery(qs)
    }


    QScriptList upsert(@DelegatesTo(value = UpsertQuery, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        QContext qContext = createContext()
        UpsertQuery qu = new UpsertQuery(qContext)

        def code = closure.rehydrate(qu, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        qu.createScripts(qContext, session)
    }

    QScriptList insertOrLoad(@DelegatesTo(value = InsertOrQuery, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        QContext qContext = createContext()
        InsertOrQuery qu = new InsertOrQuery(qContext)

        def code = closure.rehydrate(qu, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        qu.createScripts(qContext, session)
    }

    QResultProxy valueTable(Collection<Object> values, String colAlias = null) throws NyException {
        ValueTable vt = new ValueTable(values: values, columnAlias: colAlias)

        QContext context = createContext()
        context.translator.___valueTable(vt)
    }
    Object cte(@DelegatesTo(value = CTE, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        QContext qContext = createContext()
        CTE qcte = new CTE(qContext)

        def code = closure.rehydrate(qcte, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        def queries = qcte._ctx.translator.___cteQuery(qcte)
        if (queries.size() == 1) {
            return queries.get(0)
        } else {
            QScriptList scriptList = new QScriptList()
            scriptList.scripts = []
            for (QResultProxy proxy : queries) {
                scriptList.scripts.add(new QScript(qSession: session, proxy: proxy))
            }
            return scriptList
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////
    ////
    ////   Query Part Reuse Related Commands
    ////
    ///////////////////////////////////////////////////////////////////////////////////

    QResultProxy $q(@DelegatesTo(value = QueryPart, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        QueryPart qp = new QueryPart(createContext())
        //Object qp = assignTraits(queryPart)

        def code = closure.rehydrate(qp, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        qp._ctx.translator.___partQuery(qp)
    }

    ///////////////////////////////////////////////////////////////////////////////////
    ////
    ////   DDL Related Commands
    ////
    ///////////////////////////////////////////////////////////////////////////////////

    @CompileStatic
    QScriptList drop(String table, boolean isTemp = true, boolean ifExist = false) {
        if (isTemp) {
            ddl {
                DROP_TEMP_TABLE(table, ifExist)
            }
        } else {
            ddl {
                DROP_TABLE(table, ifExist)
            }
        }
    }

    QScriptList ddl(@DelegatesTo(value = DDL, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        DDL activeDDL = new DDL(session)

        def code = closure.rehydrate(activeDDL, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        activeDDL.createScripts()
    }

    ///////////////////////////////////////////////////////////////////////////////////
    ////
    ////   Transaction Related Commands
    ////
    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Enables auto commit at the end of transaction.
     * @param autoCommit status of auto committing.
     * @return this DSL instance.
     */
    DSL AUTO_COMMIT(boolean autoCommit=true) {
        currentTransactionAutoCommit = autoCommit
        this
    }

    /**
     * Starts a new transaction.
     *
     * Note: No sub-transactions are allowed within a transaction
     *
     * @param autoCommit auto commit on successful transaction.
     * @param closure transaction content.
     * @return this same DSL instance
     */
    @SuppressWarnings('CatchThrowable')
    DSL TRANSACTION(@DelegatesTo(value = DSL, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        try {
            session.executor.startTransaction()

            def code = closure.rehydrate(this, this, this)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()

            if (currentTransactionAutoCommit) {
                COMMIT()
            }

        } catch (Throwable ex) {
            LOGGER.error('Error occurred while in transaction!', ex)
            ROLLBACK()

        } finally {
            session.executor.done()
            currentTransactionAutoCommit = false
        }
        this
    }

    /**
     * Commit the changes done within this active transaction.
     *
     * @return this same DSL instance
     */
    DSL COMMIT() {
        session.executor.commit()
        this
    }

    /**
     * Creates a new checkpoint at this point. So, you will probably can rollback
     * when an error occurred.
     *
     * @return a reference to the newly created checkpoint at this stage.
     */
    def CHECKPOINT() {
        session.executor.checkPoint()
    }

    /**
     * Rollback the transaction to the given checkpoint, or to the beginning if no checkpoint
     * has been given.
     *
     * @param checkPoint checkpoint reference to rollback
     * @return this same DSL instance
     */
    DSL ROLLBACK(Object checkPoint=null) {
        session.executor.rollback(checkPoint)
        this
    }

    ///////////////////////////////////////////////////////////////////////////////////
    ////
    ////   Log Related Commands
    ////
    ///////////////////////////////////////////////////////////////////////////////////

    DSL $LOG(Object msg) {
        $LOG(String.valueOf(msg))
    }

    DSL $LOG(String message) {
        LOGGER.debug('[' + session.configurations.getName() + '@' + session.currentActiveScript() + ']' + message)
        this
    }

    ///////////////////////////////////////////////////////////////////////////////////
    ////
    ////   Common Commands
    ////
    ///////////////////////////////////////////////////////////////////////////////////

    AParam PARAM(String name, AParam.ParamScope scope=null, String mappingName=null) {
        QUtils.createParam(name, scope, mappingName)
    }

    ParamList PARAMLIST(String name) {
        new ParamList(__name: name)
    }

    private QContext createContext(String db=null) {
        String dbName = db ?: session.dbFactory.dbName()
        new QContext(translator: session.dbFactory.createTranslator(),
                translatorName: dbName,
                ownerSession: session)
    }

    DSL $DSL = this

    Map $SESSION

    String $DB

}
