package com.virtusa.gto.insight.nyql

/**
 * @author IWEERARATHNA
 */
class QueryPart extends Query {

    Assign _assigns
    List<Object> _allProjections

    QueryPart(QContext contextParam) {
        super(contextParam)
    }

    QueryPart EXPECT(Table table) {
        if (table.__aliasDefined()) {
            if (!_ctx.tables.containsKey(table.__alias)) {
                _ctx.tables.put(table.__alias, table)
            }
        } else {
            if (!_ctx.tables.containsKey(table.__name)) {
                _ctx.tables.containsKey(table.__name)
            }
        }
        return this
    }

    QueryPart FETCH(Object... columns) {
        if (_allProjections == null) {
            _allProjections = new LinkedList<>()
        }
        for (Object col : columns) {
            if (col instanceof List) {
                _allProjections.addAll(col)
            } else {
                _allProjections.add(col)
            }
        }

        return this
    }

    QueryPart JOIN(closure) {
        def code = closure.rehydrate(this, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        sourceTbl = code()
        return this
    }

    QueryPart JOIN(Table startTable, closure) {
        JoinClosure joinClosure = new JoinClosure(_ctx, startTable)

        def code = closure.rehydrate(joinClosure, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        sourceTbl = joinClosure.activeTable
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
