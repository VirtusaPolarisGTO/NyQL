package com.virtusa.gto.insight.nyql.model

import com.virtusa.gto.insight.nyql.QExecutor

import java.util.concurrent.ConcurrentHashMap

/**
 * @author IWEERARATHNA
 */
final class QExecutorRegistry {

    private final Map<String, QExecutor> registry = new ConcurrentHashMap<>()
    private QExecutor defExec = null

    private QExecutorRegistry() {}

    public static QExecutorRegistry getInstance() {
        return Holder.INSTANCE
    }

    public QExecutor register(String name, QExecutor repository, boolean makeDefault=true) {
        registry.put(name, repository)
        if (makeDefault) {
            defExec = repository
        }
        return repository
    }

    public QExecutor defaultExecutor() {
        return defExec
    }

    private static class Holder {
        private static final QExecutorRegistry INSTANCE = new QExecutorRegistry()
    }


}
