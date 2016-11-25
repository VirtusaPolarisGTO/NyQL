package com.virtusa.gto.nyql.engine.pool.impl

import com.virtusa.gto.nyql.configs.ConfigKeys
import com.virtusa.gto.nyql.engine.pool.QJdbcPool
import com.virtusa.gto.nyql.exceptions.NyException
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets
import java.sql.Connection

/**
 * Available configurations are mentioned here.
 *
 * https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby
 *
 * @author IWEERARATHNA
 */
class QHikariPool implements QJdbcPool {

    private static final String DEF_POOL_NAME = 'NyPool'

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
        if (System.getProperty(ConfigKeys.SYS_JDBC_DRIVER) != null) {
            config.setDriverClassName(System.getProperty(ConfigKeys.SYS_JDBC_DRIVER))
        } else if (options.containsKey(ConfigKeys.JDBC_DRIVER_CLASS_KEY)) {
            config.setDriverClassName(String.valueOf(options.jdbcDriverClass))
        }
        config.setJdbcUrl(System.getProperty(ConfigKeys.SYS_JDBC_URL, String.valueOf(options.url)))
        config.setUsername(System.getProperty(ConfigKeys.SYS_JDBC_USERNAME, String.valueOf(options.username)))

        // read password correctly
        if (System.getProperty(ConfigKeys.SYS_JDBC_PASSWORD_ENC) != null) {
            config.setPassword(new String(Base64.decoder.decode(System.getProperty(ConfigKeys.SYS_JDBC_PASSWORD_ENC)),
                    StandardCharsets.UTF_8))
        } else if (System.getProperty(ConfigKeys.SYS_JDBC_PASSWORD) != null) {
            config.setPassword(System.getProperty(ConfigKeys.SYS_JDBC_PASSWORD))
        } else if (options.passwordEnc) {
            config.setPassword(new String(Base64.decoder.decode(String.valueOf(options.passwordEnc)), StandardCharsets.UTF_8))
        } else {
            config.setPassword(String.valueOf(options.password))
        }

        config.setPoolName(DEF_POOL_NAME)

        LOGGER.debug('JDBC Hikari Pool Configurations:')
        LOGGER.debug('  - JDBC ' + config.getJdbcUrl() + ', @user: ' + config.getUsername())

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
