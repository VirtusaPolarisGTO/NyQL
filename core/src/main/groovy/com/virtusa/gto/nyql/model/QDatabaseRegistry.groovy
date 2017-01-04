package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.db.QDbFactory

import java.util.concurrent.ConcurrentHashMap
/**
 * @author IWEERARATHNA
 */
final class QDatabaseRegistry {

    private Map<String, QDbFactory> factoryRegistry = new ConcurrentHashMap<>()

    private QDatabaseRegistry() {}

    QDbFactory getDbFactory(String dbName) {
        factoryRegistry.get(dbName)
    }

    void register(QDbFactory factory) {
        factoryRegistry.put(factory.dbName(), factory)
    }

    static QDatabaseRegistry newInstance() {
        new QDatabaseRegistry()
    }
}
