package com.virtusa.gto.nyql.db.postgre

import com.virtusa.gto.nyql.configs.ConfigKeys
import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.db.QDbFactory
import com.virtusa.gto.nyql.db.QTranslator
import com.virtusa.gto.nyql.db.SqlMisc
import com.virtusa.gto.nyql.db.TranslatorOptions
import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.model.DbInfo
import groovy.transform.CompileStatic
/**
 * @author IWEERARATHNA
 */
class PostgreFactory implements QDbFactory {

    private static final String PG = 'pg'
    private static final String PG_DRIVER_CLZ = 'org.postgresql.Driver'
    private static final String PG_DATA_SOURCE_NAME = 'org.postgresql.ds.PGSimpleDataSource'
    private static final String PG_KEYWORDS_LOCATION = 'com/virtusa/gto/nyql/db/postgre/keywords.json'
    private Postgres postgres

    @Override
    void init(Configurations nyConfigs, DbInfo dbInfo) throws NyConfigurationException {
        // load postgre keyword list
        Collection<String> keywords = loadKeywords(nyConfigs)

        postgres = new Postgres(new TranslatorOptions(keywords))
    }

    private static Set<String> loadKeywords(Configurations nyConfigs) {
        Map props = nyConfigs.getAllProperties()
        String loc = props.get(ConfigKeys.QUERIES_ROOT)?.get(PG)?.get(ConfigKeys.QUERIES_KEYWORDS)
        if (loc == null) {
            loc = props.get(ConfigKeys.QUERIES_ROOT)?.get(ConfigKeys.QUERIES_KEYWORDS)
        }
        File file = null
        if (loc != null) {
            file = new File(new File(String.valueOf(props.get(ConfigKeys.LOCATION_KEY))), loc)
        }
        return SqlMisc.loadKeywords(PG_KEYWORDS_LOCATION, file)
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
