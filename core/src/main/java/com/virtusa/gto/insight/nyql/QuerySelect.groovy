package com.virtusa.gto.insight.nyql
/**
 * @author Isuru Weerarathna
 */
class QuerySelect extends Query {

    List<Object> orderBy = null
    List<Object> groupBy = null
    Where groupHaving = null
    List<Object> projection = null
    Table _intoTable = null
    Table _joiningTable = null
    boolean _distinct = false
    def offset

    QuerySelect(QContext contextParam) {
        super(contextParam)
    }

    QuerySelect DISTINCT_FETCH(Object... columns) {
        _distinct = true
        return FETCH(columns)
    }

    def TARGET(Table table) {
        sourceTbl = table
        return this
    }

    def TARGET() {
        return sourceTbl
    }

    def INTO(Table table) {
        _intoTable = table
        return this
    }

    def JOIN(closure) {
        def code = closure.rehydrate(this, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        _joiningTable = code()
        return this
    }

    def JOINING(closure) {
        JoinClosure joinClosure = new JoinClosure(_ctx, sourceTbl)

        def code = closure.rehydrate(joinClosure, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

    }

    def ORDER_BY(Object... columns) {
        orderBy = new ArrayList<>()
        orderBy.addAll(columns)
        return this
    }

    def GROUP_BY(Object... columns) {
        groupBy = new ArrayList<>()
        groupBy.addAll(columns)
        return this
    }

    def HAVING(closure) {
        Where whr = new Where(_ctx)

        def code = closure.rehydrate(whr, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        groupHaving = whr
        return this
    }

    def FETCH(Object... columns) {
        projection = new ArrayList<>()
        if (columns != null) {
            projection.addAll(columns)
        }
        return this
    }

    def OFFSET(Object start) {
        offset = start
        return this
    }

    def TOP(Object count) {
        return OFFSET(0).LIMIT(count)
    }

}
