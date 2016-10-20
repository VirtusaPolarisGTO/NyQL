package com.virtusa.gto.insight.nyql.engine.pool.impl

import com.virtusa.gto.insight.nyql.engine.pool.QJdbcPool
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.Connection

/**
 * Available configurations are mentioned here.
 *
 * https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby
 *
 * @author IWEERARATHNA
 */
class QHikariPool implements QJdbcPool {

    private static final Logger LOGGER =  LoggerFactory.getLogger(QHikariPool)
    private HikariDataSource hikari = null
    private final Object hikariLock = new Object()

    @Override
    Connection getConnection() throws NyException {
        LOGGER.debug('Requesting a new connection')
        return hikari.getConnection()
    }

    @Override
    void init(Map options) throws NyException {
        HikariConfig config = new HikariConfig();
        //config.setDataSourceClassName(String.valueOf(options.dataSourceClassName))
        if (options.containsKey("jdbcDriverClass")) {
            config.setDriverClassName(String.valueOf(options.jdbcDriverClass))
        }
        config.setJdbcUrl(String.valueOf(options.url))
        config.setUsername(String.valueOf(options.username))
        config.setPassword(String.valueOf(options.password))
        config.setPoolName("NyPool")

        if (options.pooling) {
            Map poolingConfigs = options.pooling as Map
            Properties properties = new Properties()

            poolingConfigs.each { k, v ->
                if (config.hasProperty(String.valueOf(k))) {
                    config."$k" = v
                } else {
                    properties.put(k, v)
                }
            }

            config.setDataSourceProperties(properties)
        }

        synchronized (hikariLock) {
            hikari = new HikariDataSource(config)
        }
    }

    @Override
    void shutdown() throws NyException {
        synchronized (hikariLock) {
            hikari.close()
        }
    }
}
