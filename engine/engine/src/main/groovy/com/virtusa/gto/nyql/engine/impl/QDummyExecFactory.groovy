package com.virtusa.gto.nyql.engine.impl

import com.virtusa.gto.nyql.model.DbInfo
import com.virtusa.gto.nyql.model.QExecutor
import com.virtusa.gto.nyql.model.QExecutorFactory

/**
 * @author IWEERARATHNA
 */
class QDummyExecFactory implements QExecutorFactory {
    @Override
    DbInfo init(Map options) {
        DbInfo.UNRESOLVED
    }

    @Override
    QExecutor create() {
        new QDummyExecutor()
    }

    @Override
    QExecutor createReusable() {
        new QDummyExecutor()
    }

    @Override
    void shutdown() {

    }
}
