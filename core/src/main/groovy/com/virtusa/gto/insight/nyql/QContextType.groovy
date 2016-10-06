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
    INSIDE_FUNCTION,

    UPDATE_FROM,
    UPDATE_JOIN,
    UPDATE_SET,

    FROM,

    DDL,

    INTO,

    UNKNOWN

}