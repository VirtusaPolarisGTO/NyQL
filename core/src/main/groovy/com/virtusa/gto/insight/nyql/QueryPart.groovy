package com.virtusa.gto.insight.nyql

/**
 * @author IWEERARATHNA
 */
class QueryPart extends Query {

    Assign _assigns
    List<Object> _allProjections
    List<Object> _intoColumns

    QueryPart(QContext contextParam) {
        super(contextParam)
    }

    QueryPart COLUMNS(Object... columns) {
        if (_intoColumns == null) {
            _intoColumns = [] as Queue
        }

        for (Object col : columns) {
            if (col instanceof List) {
                _intoColumns.addAll(col)
            } else {
                _intoColumns.add(col)
            }
        }
        this
    }

    QueryPart FETCH(Object... columns) {
        if (_allProjections == null) {
            _allProjections = [] as Queue
        }
        for (Object col : columns) {
            if (col instanceof List) {
                _allProjections.addAll(col)
            } else {
                _allProjections.add(col)
            }
        }

        this
    }

    QueryPart JOIN(Table startTable, closure) {
        JoinClosure joinClosure = new JoinClosure(_ctx, startTable)

        def code = closure.rehydrate(joinClosure, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        sourceTbl = joinClosure.activeTable
        this
    }

    QueryPart SET(closure) {
        Assign ass = new Assign(_ctx, this)

        def code = closure.rehydrate(ass, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        _assigns = ass
        this
    }
}
