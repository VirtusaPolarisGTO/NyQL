package com.virtusa.gto.insight.nyql.db.mysql

import com.virtusa.gto.insight.nyql.db.QDbFactory
import com.virtusa.gto.insight.nyql.db.QTranslator

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
    private final MySql mySql = new MySql()
    private final List<Class<?>> traits = []

    /**
     * Returns text 'mysql'.
     *
     * @return mysql text.
     */
    @Override
    String dbName() {
        return DB_NAME
    }

    /**
     * Returns JDBC data source full classname.
     *
     * @return JDBC data source full classname.
     */
    @Override
    String dataSourceClassName() {
        return DATA_SOURCE_CLASS_NAME
    }

    /**
     * Creates a new translator. MySQL gives the same instance
     * every time, because translator is thread-safe.
     *
     * @return mysql translator instance.
     */
    @Override
    QTranslator createTranslator() {
        return mySql
    }

    /**
     * List of dynamic traits to apply for translator before parsing.
     *
     * @return mysql does not return any traits.
     */
    @Override
    List<Class<?>> createTraits() {
        return traits
    }

    /**
     * JDBC driver class name.
     *
     * @return jdbc driver class name.
     */
    @Override
    String driverClassName() {
        return JDBC_CLASS_NAME
    }
}
