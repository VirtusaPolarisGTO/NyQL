package com.virtusa.gto.nyql.db.postgre

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.db.QDbFactory
import com.virtusa.gto.nyql.db.QTranslator
import com.virtusa.gto.nyql.exceptions.NyConfigurationException

/**
 * @author IWEERARATHNA
 */
class PostgreFactory implements QDbFactory {
    @Override
    void init(Configurations nyConfigs) throws NyConfigurationException {
        // nothing to pre-configure in postgre
    }

    @Override
    String dbName() {
        return "pg"
    }

    @Override
    QTranslator createTranslator() {
        return new Postgres()
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
