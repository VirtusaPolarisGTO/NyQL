package com.virtusa.gto.nyql.model

import java.util.concurrent.ConcurrentHashMap

/**
 * @author IWEERARATHNA
 */
final class QExecutorRegistry {

    private final Map<String, QExecutorFactory> registry = new ConcurrentHashMap<>()
    private QExecutorFactory defExec = null

    private QExecutorRegistry() {}

    static QExecutorRegistry getInstance() {
        Holder.INSTANCE
    }

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

    private static class Holder {
        private static final QExecutorRegistry INSTANCE = new QExecutorRegistry()
    }


}
