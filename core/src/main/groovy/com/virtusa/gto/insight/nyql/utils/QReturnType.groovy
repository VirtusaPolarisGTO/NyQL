package com.virtusa.gto.insight.nyql.utils

/**
 * @author IWEERARATHNA
 */
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
