package com.virtusa.gto.insight.nyql.engine.impl

import com.virtusa.gto.insight.nyql.model.QExecutor
import com.virtusa.gto.insight.nyql.engine.impl.pool.QHikariPool
import com.virtusa.gto.insight.nyql.engine.impl.pool.QJdbcPool
import com.virtusa.gto.insight.nyql.model.QExecutorFactory

/**
 * Factory responsible of creating JDBC executors per invocation or per thread.
 *
 * @author IWEERARATHNA
 */
class QJdbcExecutorFactory implements QExecutorFactory {

    private QJdbcPool jdbcPool = new QHikariPool()

    @Override
    void init(Map options) {
        jdbcPool.init(options)
    }

    @Override
    QExecutor create() {
        return new QJdbcExecutor(jdbcPool)
    }

    @Override
    QExecutor createReusable() {
        return new QJdbcExecutor(jdbcPool, true)
    }

    @Override
    void shutdown() {
        jdbcPool.shutdown()
    }
}
