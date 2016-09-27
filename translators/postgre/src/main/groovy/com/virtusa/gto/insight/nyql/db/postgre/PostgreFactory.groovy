package com.virtusa.gto.insight.nyql.db.postgre

import com.virtusa.gto.insight.nyql.db.QDbFactory
import com.virtusa.gto.insight.nyql.db.QTranslator

/**
 * @author IWEERARATHNA
 */
class PostgreFactory implements QDbFactory {
    @Override
    String dbName() {
        return "pg"
    }

    @Override
    QTranslator createTranslator() {
        return new Postgres()
    }

    @Override
    List<Class<?>> createTraits() {
        return [PostgresFunctions]
    }

    @Override
    String driverClassName() {
        return "org.postgresql.Driver"
    }

    @Override
    String dataSourceClassName() {
        return "org.postgresql.ds.PGSimpleDataSource"
    }
}
