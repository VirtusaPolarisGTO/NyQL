package com.virtusa.gto.nyql

import com.virtusa.gto.nyql.utils.QueryCombineType
import groovy.transform.CompileStatic

/**
 * @author iweerarathna
 */
@CompileStatic
class WithClosure {

    QuerySelect anchor
    QuerySelect recursion

    QueryCombineType combineType = QueryCombineType.UNION
    QContext _ctx

    WithClosure(QContext context) {
        _ctx = context
    }

    WithClosure UNION() {
        combineType = QueryCombineType.UNION_DISTINCT
        this
    }

    WithClosure UNION_ALL() {
        combineType = QueryCombineType.UNION
        this
    }

    WithClosure ANCHOR(@DelegatesTo(value = QuerySelect, strategy = Closure.DELEGATE_ONLY) Closure body) {
        QuerySelect querySelect = new QuerySelect(_ctx)

        def code = body.rehydrate(querySelect, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        anchor = querySelect
        this
    }

    WithClosure RECURSION(@DelegatesTo(value = QuerySelect, strategy = Closure.DELEGATE_ONLY) Closure body) {
        QuerySelect querySelect = new QuerySelect(_ctx)

        def code = body.rehydrate(querySelect, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        recursion = querySelect
        this
    }
}
