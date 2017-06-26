package com.virtusa.gto.nyql.engine.impl

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.model.DbInfo
import com.virtusa.gto.nyql.model.QExecutor
import com.virtusa.gto.nyql.model.QExecutorFactory

/**
 * @author IWEERARATHNA
 */
class QDummyExecFactory implements QExecutorFactory {

    private Configurations nyqlConfigs

    @Override
    DbInfo init(Map options, Configurations configurations) {
        nyqlConfigs = configurations
        DbInfo.UNRESOLVED
    }

    @Override
    QExecutor create() {
        new QDummyExecutor(nyqlConfigs)
    }

    @Override
    QExecutor createReusable() {
        new QDummyExecutor(nyqlConfigs)
    }

    @Override
    void shutdown() {

    }
}
