package com.virtusa.gto.insight.nyql.model

import com.virtusa.gto.insight.nyql.DSLContext
import com.virtusa.gto.insight.nyql.db.QDbFactory
import com.virtusa.gto.insight.nyql.exceptions.NyException

import java.util.concurrent.ConcurrentHashMap

/**
 * @author IWEERARATHNA
 */
final class QDatabaseRegistry {

    private Map<String, QDbFactory> factoryRegistry = new ConcurrentHashMap<>()

    private Map<String, DSLContext> dslContextMap = new ConcurrentHashMap<>()

    private QDatabaseRegistry() {}

    static QDatabaseRegistry getInstance() {
        Holder.INSTANCE
    }

    QDbFactory getDbFactory(String dbName) {
        factoryRegistry.get(dbName)
    }

    DSLContext load(String dbName) throws NyException {
        if (dslContextMap.containsKey(dbName)) {
            dslContextMap[dbName]
        } else {
            QDbFactory factory = factoryRegistry[dbName]
            if (factory == null) {
                throw new NyException("There are no registered db implementation found for '$dbName'!")
            }

            DSLContext dslContext = DSLContext.register(dbName)
            dslContext.activeFactory = factory

            dslContextMap.put(dbName, dslContext)
            dslContext
        }
    }

    void register(QDbFactory factory) {
        factoryRegistry.put(factory.dbName(), factory)
    }

    private static class Holder {
        private static final QDatabaseRegistry INSTANCE = new QDatabaseRegistry()
    }


}
