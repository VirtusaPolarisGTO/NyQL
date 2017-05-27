package com.virtusa.gto.nyql

import com.virtusa.gto.nyql.utils.QReturnType
import groovy.transform.CompileStatic

/**
 * @author Isuru Weerarathna
 */
@CompileStatic
class QueryInsert extends QuerySelect {

    Map<String, Object> _data = new LinkedHashMap<>()
    Assign _assigns = null

    QueryInsert(QContext contextParam) {
        super(contextParam)
    }

    QueryInsert DATA(Map args) {
        args.each { k, v -> _data.put(String.valueOf(k), v) }
        return this
    }

    QueryInsert DATA(Map... args) {
        if (args != null) {
            for (Map map : args) {
                DATA(map)
            }
        }
        this
    }

    QueryInsert CELL_DATA(String columnName, Object val) {
        _data.put(columnName, val)
        return this
    }

    QueryInsert SET(@DelegatesTo(value = Assign, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        Assign ass = new Assign(_ctx, this)

        def code = closure.rehydrate(ass, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        _assigns = ass
        this
    }

    QueryInsert RETURN_KEYS() {
        returnType = QReturnType.KEYS
        return this
    }

}
