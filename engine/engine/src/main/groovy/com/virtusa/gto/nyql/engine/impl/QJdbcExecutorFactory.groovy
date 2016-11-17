package com.virtusa.gto.nyql.engine.impl

import com.virtusa.gto.nyql.engine.pool.QJdbcPool
import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.model.QExecutor
import com.virtusa.gto.nyql.model.QExecutorFactory
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * Factory responsible of creating pooled JDBC executors per invocation or per thread.
 *
 * @author IWEERARATHNA
 */
@CompileStatic
class QJdbcExecutorFactory implements QExecutorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(QJdbcExecutorFactory)

    private DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT

    private QJdbcPool jdbcPool

    @Override
    void init(Map options) throws NyConfigurationException {
        loadQueryParameterInfo(options)

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

    private void loadQueryParameterInfo(Map options) {
        if (options.parameters) {
            String tsFormat = options.parameters['inputTimestampFormat']
            if (tsFormat != null && !tsFormat.isEmpty()) {
                String tsLocale = options.parameters['inputTimestampLocale']
                formatter = DateTimeFormatter.ofPattern(tsFormat,
                        tsLocale == null ? Locale.default : Locale.forLanguageTag(tsLocale))

                LOGGER.debug('JDBC executor uses time-format ' + tsFormat + ' in ' + (tsLocale ?: 'system default') + ' locale.')
            }
        }
    }

    @Override
    QExecutor create() {
        new QJdbcExecutor(jdbcPool).setOwner(this)
    }

    @Override
    QExecutor createReusable() {
        new QJdbcExecutor(jdbcPool, true).setOwner(this)
    }

    @Override
    void shutdown() {
        jdbcPool.shutdown()
    }

    Timestamp convertTimestamp(String value, String tsFormat = null) {
        if (tsFormat == null) {
            Timestamp.from(Instant.parse(value))
        } else {
            Timestamp.from(Instant.from(formatter.parse(value)))
        }
    }

    static Date convertToDate(String value) {
        Date.valueOf(value)
    }
}
