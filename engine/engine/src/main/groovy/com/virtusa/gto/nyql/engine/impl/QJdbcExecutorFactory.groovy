package com.virtusa.gto.nyql.engine.impl

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.engine.pool.QJdbcPool
import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.model.DbInfo
import com.virtusa.gto.nyql.model.QExecutor
import com.virtusa.gto.nyql.model.QExecutorFactory
import com.virtusa.gto.nyql.utils.ReflectUtils
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
        LOGGER.info("Initializing database connection...")
        nyqlConfigs = configurations
        if (options.pooling) {
            String implClz = String.valueOf(options.pooling['impl'] ?: '')
            if (!implClz.isEmpty()) {
                jdbcPool = loadPool(implClz)
            } else {
                throw new NyConfigurationException("JDBC pooling implementation has not been specified under key 'impl'!")
            }
            jdbcPool.init(options, configurations)
            return getDatabaseInfo()
        } else {
            throw new NyConfigurationException(this.class.getName() + ' is for producing pooled jdbc executors. ' +
                    'If you want to use non-pooled jdbc executor use different implementation!')
        }
    }

    private static QJdbcPool loadPool(String implName) {
        ClassLoader classLoader = Thread.currentThread().contextClassLoader
        def services = ReflectUtils.findServices(QJdbcPool, classLoader)
        for (QJdbcPool pool : services) {
            LOGGER.info("Found pool implementation: ${pool.getName()}")
            if (pool.getName() == implName) {
                return pool
            }
        }

        LOGGER.warn("No matching pool implementation found for '${implName}'!")
        // try loading class from classpath
        try {
            LOGGER.warn('Trying to initialize pool implementation: [' + implName + '] using classpath... Use pool id instead next time.')
            return (QJdbcPool) classLoader.loadClass(implName).newInstance()
        } catch (ClassNotFoundException ex) {
            throw new NyConfigurationException('JDBC pool implementation not found! ' + implName, ex)
        }
    }

    private DbInfo getDatabaseInfo() throws NyConfigurationException {
        try {
            return DbInfo.deriveFromConnection(jdbcPool.getConnection())
        } catch (NyConfigurationException ex) {
            throw ex
        } catch (Exception ex) {
            throw new NyConfigurationException("Error occurred while retrieving database information!", ex)
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
        if (jdbcPool != null) {
            jdbcPool.shutdown()
        }
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
