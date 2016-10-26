package com.virtusa.gto.nyql.utils

import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
enum QueryType {

    /**
     * A select query.
     */
    SELECT,

    /**
     * An insert query.
     */
    INSERT,

    /**
     * Bulk insert query.
     */
    BULK_INSERT,

    /**
     * Update query.
     */
    UPDATE,

    /**
     * Delete query.
     */
    DELETE,

    /**
     * Call of stored procedure or function.
     */
    DB_FUNCTION,

    /**
     * Any query which changes the schema.
     */
    SCHEMA_CHANGE,

    /**
     * Dynamic script which may contains many query executions.
     */
    SCRIPT,

    /**
     * Part of a query which can be reusable.
     */
    PART,

    /**
     * Unknown type.
     */
    UNKNOWN

}