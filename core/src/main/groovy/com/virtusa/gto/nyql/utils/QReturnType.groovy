package com.virtusa.gto.nyql.utils

import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
enum QReturnType {

    /**
     * Returns result of the query.
     */
    RESULT,

    /**
     * Returns changed keys because of the query.
     */
    KEYS,

    /**
     * Returns total number of updated rows.
     */
    COUNT

}
