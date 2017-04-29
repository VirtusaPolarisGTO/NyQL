package com.virtusa.gto.nyql

import groovy.transform.CompileStatic

/**
 * @author Isuru Weerarathna
 */
@CompileStatic
class QueryUpdate extends Query {

    Table _joiningTable = null
    Assign _assigns = null

    QueryUpdate(QContext contextParam) {
        super(contextParam)
    }

    Table TARGET() {
        sourceTbl
    }

    QueryUpdate TARGET(Table table) {
        sourceTbl = table
        this
    }

    QueryUpdate JOIN(Table startTable, @DelegatesTo(value = JoinClosure, strategy = Closure.DELEGATE_ONLY) Closure  closure) {
        JoinClosure joinClosure = new JoinClosure(_ctx, startTable)

        def code = closure.rehydrate(joinClosure, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        _joiningTable = joinClosure.activeTable
        this
    }

    QueryUpdate JOIN(@DelegatesTo(value = JoinClosure, strategy = Closure.DELEGATE_ONLY) Closure  closure) {
        JOIN(TARGET(), closure)
    }

    QueryUpdate SET(@DelegatesTo(value = Assign, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        Assign ass = new Assign(_ctx, this)

        def code = closure.rehydrate(ass, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        _assigns = ass
        this
    }

}
