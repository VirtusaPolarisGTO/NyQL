package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.utils.ReflectUtils
import groovy.transform.CompileStatic

import java.util.concurrent.ConcurrentHashMap

/**
 * @author iweerarathna
 */
@CompileStatic
class QMapperRegistry {

    private final Map<String, QMapperFactory> registry = new ConcurrentHashMap<>()

    private QMapperRegistry() {}

    void register(QMapperFactory mapperFactory) {
        def mapperNames = mapperFactory.supportedMappers()
        for (String mapName : mapperNames) {
            if (registry.containsKey(mapName)) {
                throw new NyConfigurationException("Duplicate mapper ids found! [${mapName}]")
            }
            registry.put(mapName, mapperFactory)
        }
    }

    Set<String> listAll() {
        registry.keySet()
    }

    QMapperFactory getMapperFactory(String name) {
        if (registry.containsKey(name)) {
            registry.get(name)
        } else {
            throw new NyConfigurationException("No mapper factory is found by name '${name}'!")
        }
    }

    QMapperRegistry discover(ClassLoader classLoader) {
        def services = ReflectUtils.findServices(QMapperFactory, classLoader)
        for (QMapperFactory factory : services) {
            register(factory)
        }
        this
    }

    static QMapperRegistry newInstance() {
        new QMapperRegistry()
    }

}
