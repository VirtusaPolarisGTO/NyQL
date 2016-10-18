package com.virtusa.gto.insight.nyql
/**
 * @author Isuru Weerarathna
 */
enum QContextType {

    SELECT,

    SELECT_PROJECTION,
    SELECT_FROM,
    CONDITIONAL,
    ORDER_BY,
    GROUP_BY,
    HAVING,
    INSIDE_FUNCTION,

    UPDATE_FROM,
    UPDATE_JOIN,
    UPDATE_SET,

    DELETE_FROM,
    DELETE_CONDITIONAL,

    FROM,

    DDL,

    INSERT_PROJECTION,
    INTO,

    UNKNOWN

}