package com.virtusa.gto.nyql

import groovy.transform.CompileStatic

/**
 * @author iweerarathna
 */
@CompileStatic
class CTE extends Query {

    List withs = []
    QuerySelect querySelect

    CTE(QContext contextParam) {
        super(contextParam)
    }

    CTE WITH(String tableName, @DelegatesTo(value = QuerySelect, strategy = Closure.DELEGATE_ONLY) Closure body) {
        WITH(tableName, [], body)
    }

    CTE WITH(String tableName, List columns, @DelegatesTo(value = QuerySelect, strategy = Closure.DELEGATE_ONLY) Closure body) {
        // define table in the context
        def tableRef = TABLE(tableName)

        QuerySelect querySelect = new QuerySelect(_ctx)

        def code = body.rehydrate(querySelect, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        Map item = [table: tableRef, cols: columns, query: querySelect]
        withs.add(item)
        this
    }

    CTE WITH_RECURSIVE(String tableName, @DelegatesTo(value = WithClosure, strategy = Closure.DELEGATE_ONLY) Closure body) {
        WITH_RECURSIVE(tableName, [], body)
    }

    CTE WITH_RECURSIVE(String tableName, List columns, @DelegatesTo(value = WithClosure, strategy = Closure.DELEGATE_ONLY) Closure body) {
        def tableRef = TABLE(tableName)
        WITH_RECURSIVE(tableRef, columns, body)
    }

    CTE WITH_RECURSIVE(Table tableRef, List columns, @DelegatesTo(value = WithClosure, strategy = Closure.DELEGATE_ONLY) Closure body) {
        WithClosure withClosure = new WithClosure(_ctx)
        def code = body.rehydrate(withClosure, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        Map item = [table: tableRef, cols: columns, query: withClosure]
        withs.add(item)
        this
    }

    CTE SELECT(@DelegatesTo(value = QuerySelect, strategy = Closure.DELEGATE_ONLY) Closure body) {
        QContext tmpContext = new QContext(ownerSession: _ctx.ownerSession, translator: _ctx.translator,
                                            translatorName: _ctx.translatorName, ownQuery: this)
        querySelect = new QuerySelect(tmpContext)

        def code = body.rehydrate(querySelect, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        this
    }

}
