package com.virtusa.gto.insight.nyql

/**
 * @author IWEERARATHNA
 */
class QueryDelete extends Query {

    Table _joiningTable = null

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

    def JOIN(Table startTable, closure) {
        JoinClosure joinClosure = new JoinClosure(_ctx, startTable)

        def code = closure.rehydrate(joinClosure, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        _joiningTable = joinClosure.activeTable
        return this
    }

}
