package com.virtusa.gto.insight.nyql.utils

/**
* @author IWEERARATHNA
*/
enum QueryCombineType {

    /**
     * Union without considering uniqueness.
     */
    UNION,

    /**
     * Union only distinct records.
     */
    UNION_DISTINCT,

    /**
     * Returns only common records from both queries.
     */
    INTERSECT,

    /**
     * Sequential records.
     */
    SEQUENTIAL

}