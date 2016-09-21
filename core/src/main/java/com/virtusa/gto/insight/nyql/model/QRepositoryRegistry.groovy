package com.virtusa.gto.insight.nyql.model

import java.util.concurrent.ConcurrentHashMap

/**
 * @author IWEERARATHNA
 */
final class QRepositoryRegistry {

    private Map<String, QRepository> registry = new ConcurrentHashMap<>()
    private QRepository defRepo = null

    private QRepositoryRegistry() {}

    public static QRepositoryRegistry getInstance() {
        return Holder.INSTANCE
    }

    public QRepository register(String name, QRepository repository, boolean makeDefault=true) {
        registry.put(name, repository)
        if (makeDefault) {
            defRepo = repository
        }
        return repository
    }

    public QRepository defaultRepository() {
        return defRepo
    }

    private static class Holder {
        private static final QRepositoryRegistry INSTANCE = new QRepositoryRegistry()
    }

}
