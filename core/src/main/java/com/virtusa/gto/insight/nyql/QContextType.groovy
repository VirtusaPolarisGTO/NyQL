package com.virtusa.gto.insight.nyql
/**
 * @author Isuru Weerarathna
 */
enum QContextType {

    SELECT,
    FROM,
    CONDITIONAL,
    ORDER_BY,
    GROUP_BY,
    INSIDE_FUNCTION,

    UPDATE,

    DDL,

    INTO,

    UNKNOWN

}