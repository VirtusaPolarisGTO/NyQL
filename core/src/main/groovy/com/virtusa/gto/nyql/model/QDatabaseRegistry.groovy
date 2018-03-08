package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.db.QDbFactory
import com.virtusa.gto.nyql.utils.ReflectUtils
import groovy.transform.CompileStatic

import java.util.concurrent.ConcurrentHashMap
/**
 * @author IWEERARATHNA
 */
@CompileStatic
final class QDatabaseRegistry {

    private final Map<String, QDbFactory> factoryRegistry = new ConcurrentHashMap<>()

    private QDatabaseRegistry() {}

    QDbFactory getDbFactory(String dbName) {
        factoryRegistry.get(dbName)
    }

    void register(QDbFactory factory) {
        factoryRegistry.put(factory.dbName(), factory)
    }

    Collection<String> listAll() {
        factoryRegistry.keySet()
    }

    QDatabaseRegistry discover(ClassLoader classLoader = null) {
        def services = ReflectUtils.findServices(QDbFactory, classLoader)
        for (QDbFactory dbFactory : services) {
            register(dbFactory)
        }
        this
    }

    static QDatabaseRegistry newInstance() {
        new QDatabaseRegistry()
    }
}
