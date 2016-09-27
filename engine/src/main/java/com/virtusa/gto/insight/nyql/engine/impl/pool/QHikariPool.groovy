package com.virtusa.gto.insight.nyql.engine.impl.pool

import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import java.sql.Connection

/**
 * Available configurations are mentioned here.
 *
 * https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby
 *
 * @author IWEERARATHNA
 */
class QHikariPool implements QJdbcPool {

    private HikariDataSource hikari = null

    @Override
    Connection getConnection() throws NyException {
        return hikari.getConnection()
    }

    @Override
    synchronized void init(Map options) throws NyException {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName(String.valueOf(options.dataSourceClassName))
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

        hikari = new HikariDataSource(config)
    }

    @Override
    void shutdown() throws NyException {
        hikari.close()
    }
}
