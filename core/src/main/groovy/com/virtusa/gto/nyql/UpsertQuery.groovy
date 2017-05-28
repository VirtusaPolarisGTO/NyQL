package com.virtusa.gto.nyql;

import com.virtusa.gto.nyql.exceptions.NyException;
import com.virtusa.gto.nyql.model.QScript;
import com.virtusa.gto.nyql.model.QScriptList;
import com.virtusa.gto.nyql.model.QScriptListType;
import com.virtusa.gto.nyql.model.QSession;
import groovy.transform.CompileStatic;

/**
 * @author iweerarathna
 */
@CompileStatic
class UpsertQuery extends QueryUpdate implements MultiQuery {

    ReturnType returningType = ReturnType.NONE
    List<Object> returningColumns

    UpsertQuery(QContext contextParam) {
        super(contextParam);
    }

    UpsertQuery RETURN_NONE() {
        returningType = ReturnType.NONE
        this
    }

    UpsertQuery RETURN_BEFORE() {
        returningType = ReturnType.RECORD_BEFORE
        this
    }

    UpsertQuery RETURN_AFTER() {
        returningType = ReturnType.RECORD_AFTER
        this
    }

    UpsertQuery RETURN_COLUMNS(Object... columns) {
        if (returningColumns == null) {
            returningColumns = new LinkedList<>()
        }
        returningType = ReturnType.CUSTOM
        returningColumns.addAll(columns)
        this
    }

    protected QScript createSelectQuery(QContext qContext, QSession qSession) {
        QuerySelect querySelect = new QuerySelect(qContext)
        querySelect.sourceTbl = sourceTbl
        querySelect._joiningTable = _joiningTable
        querySelect.whereObj = whereObj
        querySelect.LIMIT(1)

        QResultProxy proxySelect = querySelect._ctx.translator.___selectQuery(querySelect)
        return new QScript(qSession: qSession, proxy: proxySelect)
    }

    protected QScript createInsertQuery(QContext qContext, QSession qSession) {
        QueryInsert queryInsert = new QueryInsert(qContext)
        queryInsert.sourceTbl = sourceTbl
        if (_assigns != null && _assigns.__hasAssignments()) {
            queryInsert._assigns = _assigns
        }
        queryInsert.RETURN_KEYS()

        QResultProxy proxyInsert = queryInsert._ctx.translator.___insertQuery(queryInsert)
        return new QScript(qSession: qSession, proxy: proxyInsert)
    }

    @Override
    QScriptList createScripts(QContext qContext, QSession qSession) throws Exception {
        if (whereObj == null || !whereObj.__hasClauses()) {
            throw new NyException('WHERE clause is mandatory for upsert queries!')
        }
        final QScript scriptSelect = createSelectQuery(qContext, qSession)
        final QScript scriptInsert = createInsertQuery(qContext, qSession)

        QResultProxy proxyUpdate = _ctx.translator.___updateQuery(this)
        QScript scriptUpdate = new QScript(qSession: qSession, proxy: proxyUpdate)

        QScriptList scriptList = new QScriptList()
        scriptList.scripts = [scriptSelect, scriptInsert, scriptUpdate]
        scriptList.type = QScriptListType.UPSERT
        if (returningType == ReturnType.RECORD_AFTER) {
            scriptList.scripts.add(scriptSelect)
        } else if (returningType == ReturnType.CUSTOM) {
            QuerySelect qrs = new QuerySelect(qContext)
            qrs.sourceTbl = sourceTbl
            qrs._joiningTable = _joiningTable
            qrs.whereObj = whereObj
            qrs.projection = returningColumns
            qrs.LIMIT(1)

            QResultProxy tmpProxySel = qrs._ctx.translator.___selectQuery(qrs)
            QScript tmpScriptSel = new QScript(qSession: qSession, proxy: tmpProxySel)
            scriptList.scripts.add(tmpScriptSel)
        }

        scriptList
    }

    static enum ReturnType {
        RECORD_BEFORE,
        RECORD_AFTER,
        CUSTOM,
        NONE
    }
}
