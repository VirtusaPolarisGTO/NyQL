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
        _allProjections.addAll(columns)
        return this
    }

    QueryPart JOIN(closure) {
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
