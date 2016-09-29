package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.utils.QUtils

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

    def JOIN(Table t) {
        activeTable = INNER_JOIN(t)
        return activeTable
    }

    def INNER_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, "INNER_JOIN")
        return activeTable
    }

    def LEFT_OUTER_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, "LEFT_OUTER_JOIN")
        return activeTable
    }

    def RIGHT_OUTER_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, "RIGHT_OUTER_JOIN")
        return activeTable
    }

    def RIGHT_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, "RIGHT_JOIN")
        return activeTable
    }

    def LEFT_JOIN(Table t) {
        activeTable = QUtils.mergeJoinClauses(_ctx, activeTable, t, "LEFT_JOIN")
        return activeTable
    }

}
