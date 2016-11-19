package com.virtusa.gto.nyql.db
/**
 * Interface to implement by all supporting database vendors for NyQL.
 *
 * @author IWEERARATHNA
 */
trait QDbFactory {

    /**
     * Returns the database name of vendor.
     *
     * @return the database name.
     */
    abstract String dbName()

    /**
     * Creates a new translator. Depending on your situation you may return the same
     * instance or not in subsequent calls.
     *
     * @return a translator instance to be used by query generation.
     */
    abstract QTranslator createTranslator()

    /**
     * JDBC driver class name for this database.
     *
     * @return full jdbc driver class name.
     */
    abstract String driverClassName()

    /**
     * JDBC data source class name for this database.
     *
     * @return full data source class name.
     */
    abstract String dataSourceClassName()
}
