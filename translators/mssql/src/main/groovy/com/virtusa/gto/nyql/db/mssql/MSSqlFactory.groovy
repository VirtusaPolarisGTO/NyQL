package com.virtusa.gto.nyql.db.mssql

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.db.QDbFactory
import com.virtusa.gto.nyql.db.QTranslator
import com.virtusa.gto.nyql.db.SqlMisc
import com.virtusa.gto.nyql.exceptions.NyConfigurationException

/**
 * @author IWEERARATHNA
 */
class MSSqlFactory implements QDbFactory {

    private static final String MSSQL = 'mssql'
    private static final String MSSQL_JDBC_CLASS = 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
    private static final String MSSQL_JDBC_SOURCE = 'com.microsoft.sqlserver.jdbc.SQLServerDataSource'
    private static final String MSSQL_KEYWORD_LOCATION = 'com/virtusa/gto/nyql/db/mssql/keywords.json'
    private MSSql msSql

    @Override
    void init(Configurations nyConfigs) throws NyConfigurationException {
        // load mssql keywords
        msSql = new MSSql(loadKeywords(nyConfigs))
    }

    private static Set<String> loadKeywords(Configurations nyConfigs) {
        Map props = nyConfigs.getAllProperties()
        String loc = props.get('queries')?.get('keywordsPath')
        File file = null
        if (loc != null) {
            file = new File(new File(String.valueOf(props.get('_location'))), loc)
        }
        SqlMisc.loadKeywords(MSSQL_KEYWORD_LOCATION, file)
    }

    @Override
    String dbName() {
        MSSQL
    }

    @Override
    QTranslator createTranslator() {
        msSql
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
