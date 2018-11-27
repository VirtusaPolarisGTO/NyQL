package com.virtusa.gto.nyql.db.h2

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
 * Embedded H2 database support.
 * http://www.h2database.com/html/main.html
 *
 * @author iweerarathna
 */
class H2Factory implements QDbFactory {

    private static final String H2 = 'h2'
    private static final String H2_JDBC_CLASS = 'org.h2.Driver'
    private static final String H2_JDBC_SOURCE = 'org.h2.jdbcx.JdbcDataSource'
    private static final String H2_KEYWORD_LOCATION = 'com/virtusa/gto/nyql/db/h2/keywords.json'
    private H2 h2

    @CompileStatic
    @Override
    void init(Configurations nyConfigs, DbInfo dbInfo) throws NyConfigurationException {
        // load h2 keywords
        Collection<String> keywords = loadKeywords(nyConfigs)

        h2 = new H2(new TranslatorOptions(keywords))
        h2.setConfigs(nyConfigs)
    }

    private static Set<String> loadKeywords(Configurations nyConfigs) {
        Map props = nyConfigs.getAllProperties()
        String loc = props.get(ConfigKeys.QUERIES_ROOT)?.get(H2)?.get(ConfigKeys.QUERIES_KEYWORDS)
        if (loc == null) {
            loc = props.get(ConfigKeys.QUERIES_ROOT)?.get(ConfigKeys.QUERIES_KEYWORDS)
        }
        File file = null
        if (loc != null) {
            file = new File(new File(String.valueOf(props.get(ConfigKeys.LOCATION_KEY))), loc)
        }
        return SqlMisc.loadKeywords(H2_KEYWORD_LOCATION, file)
    }

    @CompileStatic
    @Override
    String dbName() {
        H2
    }

    @CompileStatic
    @Override
    QTranslator createTranslator() {
        h2
    }

    @CompileStatic
    @Override
    String driverClassName() {
        H2_JDBC_CLASS
    }

    @CompileStatic
    @Override
    String dataSourceClassName() {
        H2_JDBC_SOURCE
    }
}
