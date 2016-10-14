package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.utils.QReturnType

/**
 * @author Isuru Weerarathna
 */
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
