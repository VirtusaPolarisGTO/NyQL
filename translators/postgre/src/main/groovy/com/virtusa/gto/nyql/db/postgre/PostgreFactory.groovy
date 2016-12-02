package com.virtusa.gto.nyql.db.postgre

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.db.QDbFactory
import com.virtusa.gto.nyql.db.QTranslator
import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import groovy.transform.CompileStatic
/**
 * @author IWEERARATHNA
 */
class PostgreFactory implements QDbFactory {

    private static final String PG = 'pg'
    private static final String PG_DRIVER_CLZ = 'org.postgresql.Driver'
    private static final String PG_DATA_SOURCE_NAME = 'org.postgresql.ds.PGSimpleDataSource'
    private static final String PG_KEYWORDS_LOCATION = ''
    private Postgres postgres

    @Override
    void init(Configurations nyConfigs) throws NyConfigurationException {
        // load postgre keyword list
        postgres = new Postgres(loadKeywords(nyConfigs))
    }

    private static Set<String> loadKeywords(Configurations nyConfigs) {
        Map props = nyConfigs.getAllProperties()
        String loc = props.get('queries')?.get('keywordsPath')
        File file = null
        if (loc != null) {
            file = new File(new File(String.valueOf(props.get('_location'))), loc)
        }
        //SqlMisc.loadKeywords(PG_KEYWORDS_LOCATION, file)
    }

    @Override
    String dbName() {
        PG
    }

    @CompileStatic
    @Override
    QTranslator createTranslator() {
        postgres
    }

    @Override
    String driverClassName() {
        PG_DRIVER_CLZ
    }

    @Override
    String dataSourceClassName() {
        PG_DATA_SOURCE_NAME
    }
}
