package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.utils.Constants
import com.virtusa.gto.nyql.utils.ReflectUtils
import groovy.transform.CompileStatic

import java.util.concurrent.ConcurrentHashMap
/**
 * @author IWEERARATHNA
 */
@CompileStatic
final class QRepositoryRegistry {

    private final Map<String, QRepository> registry = new ConcurrentHashMap<>()
    private final Map<String, QRepositoryFactory> factories = new ConcurrentHashMap<>()
    private QRepositoryFactory defFactory = null
    private QRepository defRepo = null

    private QRepositoryRegistry() {}

    QRepository register(String name, QRepository repository, boolean makeDefault=true) {
        registry.put(name, repository)
        if (makeDefault) {
            defRepo = repository
        }
        repository
    }

    private void registerFactory(QRepositoryFactory repositoryFactory, boolean makeDefault = false) {
        factories.put(repositoryFactory.getName(), repositoryFactory)
        if (makeDefault) {
            defFactory = repositoryFactory
        }
    }

    void shutdown() {
        registry.values().each {
            it.close()
        }
    }

    QRepository defaultRepository() {
        defRepo
    }

    Set<String> listAll() {
        factories.keySet()
    }

    QRepositoryFactory getRepositoryFactory(String name) {
        if (factories.containsKey(name)) {
            return factories.get(name)
        } else {
            throw new NyConfigurationException("Specified repository factory does not exist by name '${name}'!")
        }
    }

    QRepositoryRegistry discover(ClassLoader classLoader) {
        def services = ReflectUtils.findServices(QRepositoryFactory, classLoader)
        for (QRepositoryFactory repository : services) {
            registerFactory(repository, repository.getName() == Constants.DEFAULT_REPOSITORY_IMPL)
        }
        this
    }

    static QRepositoryRegistry newInstance() {
        new QRepositoryRegistry()
    }
}
