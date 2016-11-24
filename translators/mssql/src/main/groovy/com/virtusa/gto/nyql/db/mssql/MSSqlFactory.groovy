package com.virtusa.gto.nyql.db.mssql

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.db.QDbFactory
import com.virtusa.gto.nyql.db.QTranslator
import com.virtusa.gto.nyql.exceptions.NyConfigurationException

/**
 * @author IWEERARATHNA
 */
class MSSqlFactory implements QDbFactory {

    private static final String MSSQL = 'mssql'
    private static final String MSSQL_JDBC_CLASS = 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
    private static final String MSSQL_JDBC_SOURCE = 'com.microsoft.sqlserver.jdbc.SQLServerDataSource'
    private static final MSSql INSTANCE = new MSSql()

    @Override
    void init(Configurations nyConfigs) throws NyConfigurationException {
        // nothing to pre-configure in mssql
    }

    @Override
    String dbName() {
        MSSQL
    }

    @Override
    QTranslator createTranslator() {
        INSTANCE
    }

    @Override
    String driverClassName() {
        MSSQL_JDBC_CLASS
    }

    @Override
    String dataSourceClassName() {
        MSSQL_JDBC_SOURCE
    }
}
