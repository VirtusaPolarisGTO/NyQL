package com.virtusa.gto.nyql

import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QScriptList
import com.virtusa.gto.nyql.model.QScriptListType
import com.virtusa.gto.nyql.model.QSession
import groovy.transform.CompileStatic
/**
 * @author iweerarathna
 */
@CompileStatic
class InsertOrQuery extends UpsertQuery {

    InsertOrQuery(QContext contextParam) {
        super(contextParam)
    }

    @Override
    UpsertQuery RETURN_NONE() {
        throw new NyException('Not allowed to specify return type of a InsertOrLoad query!')
    }

    @Override
    UpsertQuery RETURN_BEFORE() {
        throw new NyException('Not allowed to specify return type of a InsertOrLoad query!')
    }

    @Override
    UpsertQuery RETURN_AFTER() {
        throw new NyException('Not allowed to specify return type of a InsertOrLoad query!')
    }

    @Override
    UpsertQuery RETURN_COLUMNS(Object... columns) {
        throw new NyException('Not allowed to specify return type of a InsertOrLoad query!')
    }

    @Override
    QScriptList createScripts(QContext qContext, QSession qSession) throws Exception {
        if (whereObj == null || !whereObj.__hasClauses()) {
            throw new NyException('WHERE clause is mandatory for upsert queries!')
        }
        final QScript scriptSelect = createSelectQuery(qContext, qSession)
        final QScript scriptInsert = createInsertQuery(qContext, qSession)

        QScriptList scriptList = new QScriptList()
        scriptList.scripts = [scriptSelect, scriptInsert]
        scriptList.type = QScriptListType.INSERT_OR_LOAD
        scriptList
    }
}
