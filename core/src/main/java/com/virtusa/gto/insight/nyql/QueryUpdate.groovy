package com.virtusa.gto.insight.nyql
/**
 * @author Isuru Weerarathna
 */
class QueryUpdate extends Query {

    Table _joiningTable = null
    Assign _assigns = null

    QueryUpdate(QContext contextParam) {
        super(contextParam)
    }

    def TARGET(Table table) {
        sourceTbl = table
        return this
    }

    def JOIN(closure) {
        def code = closure.rehydrate(this, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        _joiningTable = code()
        return _joiningTable
    }

    def SET(closure) {
        Assign ass = new Assign(_ctx)

        def code = closure.rehydrate(ass, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        _assigns = code()

        return this
    }

}
