package com.virtusa.gto.insight.nyql

/**
 * @author IWEERARATHNA
 */
class QueryDelete extends Query {

    QueryDelete(QContext contextParam) {
        super(contextParam)
    }

    def TARGET(Table table) {
        sourceTbl = table
        return this
    }

    def TARGET() {
        return sourceTbl
    }

}
