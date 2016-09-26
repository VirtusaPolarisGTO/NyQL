package com.virtusa.gto.insight.nyql.utils

import org.omg.CORBA.UNKNOWN

/**
 * @author IWEERARATHNA
 */
enum QueryType {

    SELECT,

    INSERT,
    BULK_INSERT,

    UPDATE,

    DELETE,

    DB_FUNCTION,

    SCHEMA_CHANGE,

    SCRIPT,

    PART,

    UNKNOWN

}