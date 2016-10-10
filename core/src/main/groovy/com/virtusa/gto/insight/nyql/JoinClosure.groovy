package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.exceptions.NySyntaxException
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.utils.QUtils
import com.virtusa.gto.insight.nyql.utils.QueryType

/**
 * @author IWEERARATHNA
 */
class JoinClosure extends AbstractClause {

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
            activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, (Table)proxy.rawObject, 'INNER_JOIN')
            return proxy.rawObject
        }
        throw new NySyntaxException('You can only import a query part having a Table reference!')
    }

    def JOIN(Table t) {
        activeTable = INNER_JOIN(t)
        return activeTable
    }

    def INNER_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, 'INNER_JOIN')
        return activeTable
    }

    def LEFT_OUTER_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, 'LEFT_OUTER_JOIN')
        return activeTable
    }

    def RIGHT_OUTER_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, 'RIGHT_OUTER_JOIN')
        return activeTable
    }

    def RIGHT_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, 'RIGHT_JOIN')
        return activeTable
    }

    def LEFT_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, 'LEFT_JOIN')
        return activeTable
    }

}
