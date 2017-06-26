package com.virtusa.gto.nyql.db.mysql

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
 * MySQL factory responsible of creating translator instances.
 *
 * MySQL translator is thread safe. So same instance can be reused among
 * multiple parallel translations.
 *
 * @author IWEERARATHNA
 */
class MySqlFactory implements QDbFactory {

    private static final String DB_NAME = 'mysql'
    private static final String DATA_SOURCE_CLASS_NAME = 'com.mysql.jdbc.jdbc2.optional.MysqlDataSource'
    private static final String JDBC_CLASS_NAME = 'com.mysql.jdbc.Driver'
    private static final String MYSQL_KEYWORD_LOCATION = 'com/virtusa/gto/nyql/db/mysql/keywords.json'
    private MySql mySql

    @CompileStatic
    @Override
    void init(Configurations nyConfigs, DbInfo dbInfo) throws NyConfigurationException {
        // load all keywords
        Collection<String> keywords = loadKeywords(nyConfigs)

        mySql = new MySql(new TranslatorOptions(keywords), dbInfo)
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
     * Returns text 'mysql'.
     *
     * @return mysql text.
     */
    @Override
    String dbName() {
        DB_NAME
    }

    /**
     * Returns JDBC data source full classname.
     *
     * @return JDBC data source full classname.
     */
    @Override
    String dataSourceClassName() {
        DATA_SOURCE_CLASS_NAME
    }

    /**
     * Creates a new translator. MySQL gives the same instance
     * every time, because translator is thread-safe.
     *
     * @return mysql translator instance.
     */
    @CompileStatic
    @Override
    QTranslator createTranslator() {
        mySql
    }

    /**
     * JDBC driver class name.
     *
     * @return jdbc driver class name.
     */
    @Override
    String driverClassName() {
        JDBC_CLASS_NAME
    }
}
