package com.virtusa.gto.insight.nyql.model

import java.util.concurrent.ConcurrentHashMap

/**
 * @author IWEERARATHNA
 */
final class QExecutorRegistry {

    private final Map<String, QExecutorFactory> registry = new ConcurrentHashMap<>()
    private QExecutorFactory defExec = null

    private QExecutorRegistry() {}

    public static QExecutorRegistry getInstance() {
        return Holder.INSTANCE
    }

    public QExecutorFactory register(String name, QExecutorFactory executorFactory, boolean makeDefault=true) {
        registry.put(name, executorFactory)
        if (makeDefault) {
            defExec = executorFactory
        }
        return executorFactory
    }

    public QExecutorFactory defaultExecutorFactory() {
        return defExec
    }

    public void shutdown() {
        registry.values().each {
            it.shutdown()
        }
    }

    private static class Holder {
        private static final QExecutorRegistry INSTANCE = new QExecutorRegistry()
    }


}
