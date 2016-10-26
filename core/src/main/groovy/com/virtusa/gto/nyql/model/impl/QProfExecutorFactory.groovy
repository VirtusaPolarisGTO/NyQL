package com.virtusa.gto.nyql.model.impl

import com.virtusa.gto.nyql.model.QExecutor
import com.virtusa.gto.nyql.model.QExecutorFactory
import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
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
