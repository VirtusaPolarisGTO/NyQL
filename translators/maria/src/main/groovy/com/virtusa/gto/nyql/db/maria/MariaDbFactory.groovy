package com.virtusa.gto.nyql.db.maria

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
 * MariaDB factory responsible of creating mariadb translators.
 *
 * @author iweerarathna
 */
class MariaDbFactory implements QDbFactory {

    private static final String DB_NAME = 'maria'
    private static final String DATA_SOURCE_CLASS_NAME = 'org.mariadb.jdbc.MariaDbDataSource'
    private static final String JDBC_CLASS_NAME = 'org.mariadb.jdbc.Driver'
    private static final String MYSQL_KEYWORD_LOCATION = 'com/virtusa/gto/nyql/db/maria/keywords.json'
    private MariaSql mariaSql

    @CompileStatic
    @Override
    void init(Configurations nyConfigs, DbInfo dbInfo) throws NyConfigurationException {
        // load all keywords
        Collection<String> keywords = loadKeywords(nyConfigs)

        mariaSql = new MariaSql(new TranslatorOptions(keywords))
    }

    private static Set<String> loadKeywords(Configurations nyConfigs) {
        Map props = nyConfigs.getAllProperties()
        String loc = props.get(ConfigKeys.QUERIES_ROOT)?.get(DB_NAME)?.get(ConfigKeys.QUERIES_KEYWORDS)
        if (loc == null) {
            loc = props.get(ConfigKeys.QUERIES_ROOT)?.get(ConfigKeys.QUERIES_KEYWORDS)
        }
        File file = null
        if (loc != null) {
            file = new File(new File(String.valueOf(props.get(ConfigKeys.LOCATION_KEY))), loc)
        }
        return SqlMisc.loadKeywords(MYSQL_KEYWORD_LOCATION, file)
    }

    /**
     * Returns text 'maria'.
     *
     * @return maria text.
     */
    @CompileStatic
    @Override
    String dbName() {
        DB_NAME
    }

    @CompileStatic
    @Override
    QTranslator createTranslator() {
        mariaSql
    }

    @CompileStatic
    @Override
    String driverClassName() {
        JDBC_CLASS_NAME
    }

    @CompileStatic
    @Override
    String dataSourceClassName() {
        DATA_SOURCE_CLASS_NAME
    }
}
