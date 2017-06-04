package com.virtusa.gto.nyql.db

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.model.DbInfo

/**
 * Interface to implement by all supporting database vendors for NyQL.
 *
 * @author IWEERARATHNA
 */
trait QDbFactory {

    /**
     * This method will be called once after nyql is configured, but just before any
     * query is executed.
     *
     * @param nyConfigs ny configuration instance.
     * @param database information
     * @throws NyConfigurationException any exception thrown while initializing this factory.
     */
    abstract void init(Configurations nyConfigs, DbInfo dbInfo) throws NyConfigurationException

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
