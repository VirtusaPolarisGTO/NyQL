package com.virtusa.gto.insight.nyql

/**
 * @author IWEERARATHNA
 */
class QueryPart extends Query {

    Assign _assigns

    QueryPart(QContext contextParam) {
        super(contextParam)
    }

    QueryPart EXPECT(Table table) {
        return this
    }

    QueryPart JOINING(closure) {
        def code = closure.rehydrate(this, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        sourceTbl = code()
        return this
    }

    QueryPart SET(closure) {
        Assign ass = new Assign(_ctx)

        def code = closure.rehydrate(ass, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        _assigns = code()

        return this
    }
}
