package com.virtusa.gto.nyql.engine.impl

import com.virtusa.gto.nyql.engine.pool.QJdbcPool
import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.model.QExecutor
import com.virtusa.gto.nyql.model.QExecutorFactory
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Factory responsible of creating pooled JDBC executors per invocation or per thread.
 *
 * @author IWEERARATHNA
 */
@CompileStatic
class QJdbcExecutorFactory implements QExecutorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(QJdbcExecutorFactory)

    private QJdbcPool jdbcPool

    @Override
    void init(Map options) throws NyConfigurationException {
        if (options.pooling) {
            String implClz = String.valueOf(options.pooling['impl'] ?: '')
            if (!implClz.isEmpty()) {
                try {
                    LOGGER.debug('Initializing pool implementation: [' + implClz + ']')
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
