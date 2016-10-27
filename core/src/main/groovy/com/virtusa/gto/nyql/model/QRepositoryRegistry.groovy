package com.virtusa.gto.nyql.model

import java.util.concurrent.ConcurrentHashMap

/**
 * @author IWEERARATHNA
 */
final class QRepositoryRegistry {

    private Map<String, QRepository> registry = new ConcurrentHashMap<>()
    private QRepository defRepo = null

    private QRepositoryRegistry() {}

    static QRepositoryRegistry getInstance() {
        Holder.INSTANCE
    }

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

    private static class Holder {
        @SuppressWarnings('UnusedPrivateField')
        private static final QRepositoryRegistry INSTANCE = new QRepositoryRegistry()
    }

}
