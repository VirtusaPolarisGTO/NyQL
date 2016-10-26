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

    def DATA(Map args) {
        args.each { k, v -> _data.put(String.valueOf(k), v) }
        return this
    }

    def RETURN_KEYS() {
        returnType = QReturnType.KEYS
        return this
    }

}
