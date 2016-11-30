package com.virtusa.gto.nyql.db.mysql

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.db.QDbFactory
import com.virtusa.gto.nyql.db.QTranslator
import com.virtusa.gto.nyql.db.SqlMisc
import com.virtusa.gto.nyql.exceptions.NyConfigurationException
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

    @Override
    void init(Configurations nyConfigs) throws NyConfigurationException {
        // load all keywords
        mySql = new MySql(loadKeywords(nyConfigs))
    }


    private static Set<String> loadKeywords(Configurations nyConfigs) {
        Map props = nyConfigs.getAllProperties()
        String loc = props.get('queries')?.get('keywordsPath')
        File file = null
        if (loc != null) {
            file = new File(new File(String.valueOf(props.get('_location'))), loc)
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
