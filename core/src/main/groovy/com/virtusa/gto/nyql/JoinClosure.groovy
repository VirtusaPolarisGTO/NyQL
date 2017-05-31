package com.virtusa.gto.nyql

import com.virtusa.gto.nyql.exceptions.NySyntaxException
import com.virtusa.gto.nyql.model.JoinType
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.utils.QUtils
import com.virtusa.gto.nyql.utils.QueryType
import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class JoinClosure extends AbstractClause {

    private static final JoinType DEF_JOIN = JoinType.INNER_JOIN

    final Table startingTable

    Table activeTable

    JoinClosure(QContext contextParam, Table targetTable) {
        super(contextParam)
        startingTable = targetTable
        activeTable = startingTable
    }

    @Override
    def $IMPORT(String scriptId) {
        QScript script = _ctx.ownerSession.scriptRepo.parse(scriptId, _ctx.ownerSession)
        def proxy = script.proxy
        if (proxy.queryType == QueryType.PART) {
            Query q = proxy.qObject as Query
            _ctx.mergeFrom(q._ctx)
            activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, (Table)proxy.rawObject, DEF_JOIN)
            return proxy.rawObject
        }
        throw new NySyntaxException('You can only import a query part having a Table reference!')
    }

    Table JOIN(Table t) {
        CROSS_JOIN(t)
    }

    Table CROSS_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, JoinType.CROSS_JOIN)
        activeTable
    }

    Table INNER_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, DEF_JOIN)
        activeTable
    }

    Table LEFT_OUTER_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, JoinType.LEFT_JOIN)
        activeTable
    }

    Table RIGHT_OUTER_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, JoinType.RIGHT_JOIN)
        activeTable
    }

    Table RIGHT_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, JoinType.RIGHT_JOIN)
        activeTable
    }

    Table LEFT_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, JoinType.LEFT_JOIN)
        activeTable
    }

    Table FULL_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, JoinType.FULL_JOIN)
        activeTable
    }
}
