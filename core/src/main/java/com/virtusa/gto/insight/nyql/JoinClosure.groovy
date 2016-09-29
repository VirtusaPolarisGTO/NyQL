package com.virtusa.gto.insight.nyql

/**
 * @author IWEERARATHNA
 */
class JoinClosure extends AbstractClause {

    final Table startingTable

    JoinClosure(QContext contextParam, Table targetTable) {
        super(contextParam)
        startingTable = targetTable
    }

    JoinClosure WITH(Table table, Closure closure) {

    }

}
