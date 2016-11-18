package com.virtusa.gto.nyql
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
    DELETE_FROM_JOIN,
    DELETE_JOIN,
    DELETE_CONDITIONAL,
    DELETE_CONDITIONAL_JOIN,

    FROM,

    DDL,

    INSERT_PROJECTION,
    INSERT_DATA,
    INTO,

    TRUNCATE,

    UNKNOWN

}