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
        throw new NyException('Not allowed to specify return type within a InsertOrLoad query!')
    }

    @Override
    UpsertQuery RETURN_BEFORE() {
        throw new NyException('Not allowed to specify return type within a InsertOrLoad query!')
    }

    @Override
    UpsertQuery RETURN_AFTER() {
        throw new NyException('Not allowed to specify return type within a InsertOrLoad query!')
    }

    @Override
    UpsertQuery RETURN_COLUMNS(Object... columns) {
        throw new NyException('Not allowed to specify return type within a InsertOrLoad query!')
    }

    @Override
    QScriptList createScripts(QContext qContext, QSession qSession) throws Exception {
        if (whereObj == null || !whereObj.__hasClauses()) {
            whereObj = convertAssignToWhere(_assigns)
        }
        final QScript scriptSelect = createSelectQuery(qContext, qSession)
        final QScript scriptInsert = createInsertQuery(qContext, qSession)

        QScriptList scriptList = new QScriptList(id: qSession?.currentActiveScript())
        scriptList.baseQuery = this
        scriptList.scripts = [scriptSelect, scriptInsert]
        scriptList.type = QScriptListType.INSERT_OR_LOAD
        scriptList
    }

    @CompileStatic
    static Where convertAssignToWhere(Assign assign) {
        Where where = new Where(assign._ctx)
        boolean andFlag = false
        for (Object item : assign.assignments) {
            if (andFlag) {
                where.AND()
            }

            if (item instanceof Assign.AnAssign) {
                Assign.AnAssign anAssign = (Assign.AnAssign)item
                if (anAssign.rightOp instanceof Table || anAssign.rightOp instanceof QResultProxy) {
                    where.IN(anAssign.leftOp, anAssign.rightOp)
                } else {
                    where.EQ(anAssign.leftOp, anAssign.rightOp)
                }
            } else {
                where.RAW(item)
            }
            andFlag = true
        }
        where
    }

}
