package com.virtusa.gto.nyql.model.impl

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.model.DbInfo
import com.virtusa.gto.nyql.model.QExecutor
import com.virtusa.gto.nyql.model.QExecutorFactory
import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class QProfExecutorFactory implements QExecutorFactory {

    private final QExecutorFactory qExecutorFactory
    private final Configurations configurations

    QProfExecutorFactory(Configurations theConfigs, QExecutorFactory executorFactory) {
        configurations = theConfigs
        qExecutorFactory = executorFactory
    }

    QExecutorFactory childFactory() {
        qExecutorFactory
    }

    @Override
    DbInfo init(Map options) {
        qExecutorFactory.init(options)
    }

    @Override
    QExecutor create() {
        new QProfExecutor(configurations, qExecutorFactory.create())
    }

    @Override
    QExecutor createReusable() {
        new QProfExecutor(configurations, qExecutorFactory.createReusable())
    }

    @Override
    void shutdown() {
        qExecutorFactory.shutdown()
    }
}
