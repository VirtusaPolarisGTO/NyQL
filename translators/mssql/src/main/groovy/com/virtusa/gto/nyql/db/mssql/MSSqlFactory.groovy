package com.virtusa.gto.nyql.db.mssql

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.db.QDbFactory
import com.virtusa.gto.nyql.db.QTranslator
import com.virtusa.gto.nyql.exceptions.NyConfigurationException

/**
 * @author IWEERARATHNA
 */
class MSSqlFactory implements QDbFactory {
    @Override
    void init(Configurations nyConfigs) throws NyConfigurationException {
        // nothing to pre-configure in mssql
    }

    @Override
    String dbName() {
        return "mssql"
    }

    @Override
    QTranslator createTranslator() {
        return new MSSql()
    }

    @Override
    String driverClassName() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver"
    }

    @Override
    String dataSourceClassName() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDataSource"
    }
}
