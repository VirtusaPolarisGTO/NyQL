package com.virtusa.gto.insight.nyql.db.mysql

import com.virtusa.gto.insight.nyql.db.QDbFactory
import com.virtusa.gto.insight.nyql.db.QTranslator

/**
 * @author IWEERARATHNA
 */
class MySqlFactory implements QDbFactory {

    private static final String DB_NAME = 'mysql'
    private static final String DATA_SOURCE_CLASS_NAME = 'com.mysql.jdbc.jdbc2.optional.MysqlDataSource'
    private static final String JDBC_CLASS_NAME = 'com.mysql.jdbc.Driver'
    private final MySql mySql = new MySql()
    private final List<Class<?>> traits = []

    @Override
    String dbName() {
        return DB_NAME
    }

    @Override
    String dataSourceClassName() {
        return DATA_SOURCE_CLASS_NAME
    }

    @Override
    QTranslator createTranslator() {
        return mySql
    }

    @Override
    List<Class<?>> createTraits() {
        return traits
    }

    @Override
    String driverClassName() {
        return JDBC_CLASS_NAME
    }
}
