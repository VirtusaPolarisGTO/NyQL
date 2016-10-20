package com.virtusa.gto.insight.nyql.engine.impl

import com.virtusa.gto.insight.nyql.engine.pool.QJdbcPool
import com.virtusa.gto.insight.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.insight.nyql.model.QExecutor
import com.virtusa.gto.insight.nyql.model.QExecutorFactory
import groovy.transform.CompileStatic

/**
 * Factory responsible of creating pooled JDBC executors per invocation or per thread.
 *
 * @author IWEERARATHNA
 */
@CompileStatic
class QJdbcExecutorFactory implements QExecutorFactory {

    private QJdbcPool jdbcPool

    @Override
    void init(Map options) throws NyConfigurationException {
        if (options.pooling) {
            String implClz = String.valueOf(options.pooling['impl'] ?: '')
            if (!implClz.isEmpty()) {
                try {
                    jdbcPool = (QJdbcPool) Thread.currentThread().contextClassLoader.loadClass(implClz).newInstance()
                } catch (ClassNotFoundException ex) {
                    throw new NyConfigurationException('JDBC pool implementation not found! ' + implClz, ex)
                }
            } else {
                throw new NyConfigurationException('JDBC pooling class has not been specified!')
            }
            jdbcPool.init(options)
        } else {
            throw new NyConfigurationException(this.class.name + ' is for producing pooled jdbc executors. ' +
                    'If you want to use non-pooled jdbc executor use another implementation!')
        }
    }

    @Override
    QExecutor create() {
        new QJdbcExecutor(jdbcPool)
    }

    @Override
    QExecutor createReusable() {
        new QJdbcExecutor(jdbcPool, true)
    }

    @Override
    void shutdown() {
        jdbcPool.shutdown()
    }
}
