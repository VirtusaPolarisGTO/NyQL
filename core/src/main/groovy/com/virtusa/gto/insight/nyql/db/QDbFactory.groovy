package com.virtusa.gto.insight.nyql.db
/**
 * @author IWEERARATHNA
 */
trait QDbFactory {

    abstract String dbName();

    abstract QTranslator createTranslator();

    abstract List<Class<?>> createTraits();

    abstract String driverClassName();

    abstract String dataSourceClassName();
}
