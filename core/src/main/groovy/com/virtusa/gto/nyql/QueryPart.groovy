package com.virtusa.gto.nyql

import com.virtusa.gto.nyql.utils.QUtils
import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class QueryPart extends Query {

    Assign _assigns
    List<Object> _allProjections
    List<Object> _intoColumns

    QueryPart(QContext contextParam) {
        super(contextParam)
    }

    QueryPart COLUMNS(Object... columns) {
        if (_intoColumns == null) {
            _intoColumns = [] as LinkedList
        }

        QUtils.expandToList(_intoColumns, columns)
        this
    }

    QueryPart FETCH(Object... columns) {
        if (_allProjections == null) {
            _allProjections = [] as LinkedList
        }

        QUtils.expandToList(_allProjections, columns)
        this
    }

    QueryPart JOIN(Table startTable, @DelegatesTo(value = JoinClosure, strategy = Closure.DELEGATE_ONLY) Closure  closure) {
        JoinClosure joinClosure = new JoinClosure(_ctx, startTable)

        def code = closure.rehydrate(joinClosure, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        sourceTbl = joinClosure.activeTable
        this
    }

    QueryPart SET(@DelegatesTo(value = Assign, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        Assign ass = new Assign(_ctx, this)

        def code = closure.rehydrate(ass, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        _assigns = ass
        this
    }
}
