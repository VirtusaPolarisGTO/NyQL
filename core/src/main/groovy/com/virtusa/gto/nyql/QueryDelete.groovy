package com.virtusa.gto.nyql

import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class QueryDelete extends Query {

    Table _joiningTable = null
    List<Column> uniqueKeys

    QueryDelete(QContext contextParam) {
        super(contextParam)
    }

    QueryDelete TARGET(Table table) {
        sourceTbl = table
        this
    }

    Table TARGET() {
        sourceTbl
    }

    QueryDelete JOIN(Table startTable, @DelegatesTo(value = JoinClosure, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        JoinClosure joinClosure = new JoinClosure(_ctx, startTable)

        def code = closure.rehydrate(joinClosure, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        _joiningTable = joinClosure.activeTable
        this
    }

    QueryDelete JOIN(@DelegatesTo(value = JoinClosure, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        JOIN(TARGET(), closure)
    }

    /**
     * This set of columns required when join is not supported in delete.
     * So, translators can generate different query using these key columns.
     *
     * @param cols set of unique columns
     * @return this
     */
    QueryDelete ON_UNIQUE_KEYS(Column... cols) {
        if (uniqueKeys == null) {
            uniqueKeys = new LinkedList<>();
        }
        uniqueKeys.addAll(cols)
        this
    }

}
