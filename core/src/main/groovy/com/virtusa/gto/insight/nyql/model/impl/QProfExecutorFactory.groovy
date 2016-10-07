package com.virtusa.gto.insight.nyql.model.impl

import com.virtusa.gto.insight.nyql.model.QExecutor
import com.virtusa.gto.insight.nyql.model.QExecutorFactory

/**
 * @author IWEERARATHNA
 */
class QProfExecutorFactory implements QExecutorFactory {

    private final QExecutorFactory qExecutorFactory

    QProfExecutorFactory(QExecutorFactory executorFactory) {
        qExecutorFactory = executorFactory
    }

    @Override
    void init(Map options) {
        qExecutorFactory.init(options)
    }

    @Override
    QExecutor create() {
        new QProfExecutor(qExecutorFactory.create())
    }

    @Override
    QExecutor createReusable() {
        new QProfExecutor(qExecutorFactory.createReusable())
    }

    @Override
    void shutdown() {
        qExecutorFactory.shutdown()
    }
}
