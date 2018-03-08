package com.virtusa.gto.nyql.engine.impl

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.engine.pool.QJdbcPool
import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.model.DbInfo
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
    private Configurations nyqlConfigs

    @Override
    String getName() {
        'jdbc'
    }

    @Override
    DbInfo init(Map options, Configurations configurations) throws NyConfigurationException {
        nyqlConfigs = configurations
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
            return getDatabaseInfo()
        } else {
            throw new NyConfigurationException(this.class.name + ' is for producing pooled jdbc executors. ' +
                    'If you want to use non-pooled jdbc executor use another implementation!')
        }
    }

    private DbInfo getDatabaseInfo() throws NyConfigurationException {
        try {
            return DbInfo.deriveFromConnection(jdbcPool.getConnection())
        } catch (NyConfigurationException ex) {
            throw ex
        } catch (Exception ex) {
            throw new NyConfigurationException("Error occurred while retreiving database information!", ex)
        }
    }

    @Override
    QExecutor create() {
        new QJdbcExecutor(jdbcPool, nyqlConfigs)
    }

    @Override
    QExecutor createReusable() {
        new QJdbcExecutor(jdbcPool, true, nyqlConfigs)
    }

    @Override
    void shutdown() {
        jdbcPool.shutdown()
    }

    /**
     * Returns the pool associated with this jdbc factory.
     *
     * @return the pool instance.
     */
    QJdbcPool getJdbcPool() {
        jdbcPool
    }
}
