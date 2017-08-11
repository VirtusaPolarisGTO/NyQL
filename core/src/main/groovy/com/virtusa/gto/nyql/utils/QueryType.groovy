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
     * Bulk update query.
     */
    BULK_UPDATE,

    /**
     * Bulk delete query.
     */
    BULK_DELETE,

    /**
     * Update query.
     */
    UPDATE,

    /**
     * Delete query.
     */
    DELETE,

    /**
     * Truncate query.
     */
    TRUNCATE,

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
     * Common table expression.
     */
    CTE,

    /**
     * Unknown type.
     */
    UNKNOWN

}