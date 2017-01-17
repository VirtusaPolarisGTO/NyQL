package com.virtusa.gto.nyql

import com.virtusa.gto.nyql.utils.QReturnType
import groovy.transform.CompileStatic

/**
 * @author Isuru Weerarathna
 */
@CompileStatic
class QueryInsert extends QuerySelect {

    Map<String, Object> _data = new LinkedHashMap<>()

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

    QueryInsert RETURN_KEYS() {
        returnType = QReturnType.KEYS
        return this
    }

}
