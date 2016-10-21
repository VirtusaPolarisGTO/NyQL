package com.virtusa.gto.insight.nyql

import groovy.transform.CompileStatic

/**
 * @author Isuru Weerarathna
 */
@CompileStatic
class QuerySelect extends Query {

    List<Object> orderBy = null
    List<Object> groupBy = null
    Where groupHaving = null
    List<Object> projection = null
    Table _intoTable = null
    List<Object> _intoColumns = null
    Table _joiningTable = null
    boolean _distinct = false
    def offset

    QuerySelect(QContext contextParam) {
        super(contextParam)
    }

    QuerySelect DISTINCT_FETCH(Object... columns) {
        _distinct = true
        FETCH(columns)
    }

    QuerySelect TARGET(Table table) {
        sourceTbl = table
        this
    }

    Table TARGET() {
        sourceTbl
    }

    QuerySelect INTO(Table table) {
        _intoTable = table
        this
    }

    QuerySelect INTO(Table table, Object... columns) {
        INTO(table)
        _intoColumns = Arrays.asList(columns)
        this
    }

    QuerySelect JOIN(Table startTable, @DelegatesTo(value = JoinClosure, strategy = Closure.DELEGATE_ONLY) Closure  closure) {
        JoinClosure joinClosure = new JoinClosure(_ctx, startTable)

        def code = closure.rehydrate(joinClosure, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        _joiningTable = joinClosure.activeTable
        this
    }

    QuerySelect ORDER_BY(Object... columns) {
        if (orderBy == null) {
            orderBy = new ArrayList<>()
        }
        orderBy.addAll(columns)
        this
    }

    QuerySelect GROUP_BY(Object... columns) {
        if (groupBy == null) {
            groupBy = new ArrayList<>()
        }
        groupBy.addAll(columns)
        this
    }

    QuerySelect HAVING(@DelegatesTo(value = Where, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        Where whr = new Where(_ctx)

        def code = closure.rehydrate(whr, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        groupHaving = whr
        this
    }

    QuerySelect FETCH(Object... columns) {
        if (projection == null) {
            projection = new LinkedList<>()
        }

        if (columns != null) {
            projection.addAll(columns)
        }
        this
    }

    QuerySelect OFFSET(Object start) {
        offset = start
        this
    }

    QuerySelect TOP(Object count) {
        OFFSET(0)
        _limit = count
        this
    }

}
