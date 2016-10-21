package com.virtusa.gto.insight.nyql.db.mssql

import com.virtusa.gto.insight.nyql.db.QDbFactory
import com.virtusa.gto.insight.nyql.db.QTranslator

/**
 * @author IWEERARATHNA
 */
class MSSqlFactory implements QDbFactory {
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
