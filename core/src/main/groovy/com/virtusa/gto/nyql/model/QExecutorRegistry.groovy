package com.virtusa.gto.nyql.model

import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

/**
 * @author IWEERARATHNA
 */
final class QExecutorRegistry {

    private DateTimeFormatter tsFormatter = DateTimeFormatter.ISO_INSTANT

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

    DateTimeFormatter getTsFormatter() {
        tsFormatter
    }

    void setTsFormatter(DateTimeFormatter tsFormatter) {
        this.tsFormatter = tsFormatter
    }

    static QExecutorRegistry newInstance() {
        new QExecutorRegistry()
    }
}
