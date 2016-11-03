package com.virtusa.gto.nyql.model

import java.util.concurrent.ConcurrentHashMap

/**
 * @author IWEERARATHNA
 */
final class QRepositoryRegistry {

    private Map<String, QRepository> registry = new ConcurrentHashMap<>()
    private QRepository defRepo = null

    private QRepositoryRegistry() {}

    QRepository register(String name, QRepository repository, boolean makeDefault=true) {
        registry.put(name, repository)
        if (makeDefault) {
            defRepo = repository
        }
        repository
    }

    void shutdown() {
        registry.values().each {
            it.close()
        }
    }

    QRepository defaultRepository() {
        defRepo
    }

    static QRepositoryRegistry newInstance() {
        new QRepositoryRegistry()
    }
}
