package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.utils.ReflectUtils
import groovy.transform.CompileStatic

import java.util.concurrent.ConcurrentHashMap
/**
 * @author IWEERARATHNA
 */
@CompileStatic
final class QExecutorRegistry {

    private final Map<String, QExecutorFactory> registry = new ConcurrentHashMap<>()
    private QExecutorFactory defExec = null

    private QExecutorRegistry() {}

    QExecutorFactory register(String name, QExecutorFactory executorFactory, boolean makeDefault=true) {
        registry.put(name, executorFactory)
        if (makeDefault) {
            defExec = executorFactory
        }
        executorFactory
    }

    QExecutorFactory defaultExecutorFactory() {
        defExec
    }

    void shutdown() {
        registry.values().each {
            it.shutdown()
        }
    }

    Set<String> listAll() {
        registry.keySet()
    }

    QExecutorFactory getExecutorFactory(String name) {
        registry.get(name)
    }

    QExecutorFactory makeDefault(String name) {
        if (registry.containsKey(name)) {
            defExec = registry.get(name)
        } else {
            throw new NyConfigurationException("Specified default executor is not found for name '${name}'!")
        }
    }

    QExecutorRegistry discover(ClassLoader classLoader) {
        def services = ReflectUtils.findServices(QExecutorFactory, classLoader)
        for (QExecutorFactory factory : services) {
            register(factory.getName(), factory, false)
        }
        this
    }

    static QExecutorRegistry newInstance() {
        new QExecutorRegistry()
    }
}
