package com.virtusa.gto.nyql.engine.pool.impl

import com.virtusa.gto.nyql.configs.ConfigKeys
import com.virtusa.gto.nyql.engine.pool.QJdbcPool
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.utils.QUtils
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import groovy.transform.CompileStatic
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
        LOGGER.trace('Requesting a new connection')
        hikari.getConnection()
    }

    @Override
    void init(Map options) throws NyException {
        HikariConfig config = new HikariConfig();
        //config.setDataSourceClassName(String.valueOf(options.dataSourceClassName))
        String jdbcDriverClass = QUtils.readEnv(ConfigKeys.SYS_JDBC_DRIVER)
        String jdbcUrl = QUtils.readEnv(ConfigKeys.SYS_JDBC_URL, String.valueOf(options.url))
        String jdbcUserName = QUtils.readEnv(ConfigKeys.SYS_JDBC_USERNAME, String.valueOf(options.username))
        String jdbcPassword = QUtils.readEnv(ConfigKeys.SYS_JDBC_PASSWORD)
        int retryCount = Integer.parseInt(QUtils.readEnv(ConfigKeys.SYS_CONNECT_RETRY_COUNT, "5"))

        if (jdbcDriverClass != null) {
            config.setDriverClassName(jdbcDriverClass)
        } else if (options.containsKey(ConfigKeys.JDBC_DRIVER_CLASS_KEY)) {
            config.setDriverClassName(String.valueOf(options.jdbcDriverClass))
        }
        config.setJdbcUrl(jdbcUrl)
        config.setUsername(jdbcUserName)

        // read password correctly
        String passEnc = QUtils.readEnv(ConfigKeys.SYS_JDBC_PASSWORD_ENC)
        if (passEnc != null) {
            config.setPassword(new String(Base64.decoder.decode(passEnc), StandardCharsets.UTF_8))
        } else if (jdbcPassword != null) {
            config.setPassword(jdbcPassword)
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

        initHikariPool(config, retryCount)
    }

    @CompileStatic
    private void initHikariPool(HikariConfig config, int retryCount) {
        int retryInterval = Integer.parseInt(QUtils.readEnv(ConfigKeys.SYS_CONNECT_RETRY_INTERVAL, "2000"))
        synchronized (hikariLock) {
            int count = 0
            while (true) {
                try {
                    if (count > 0) {
                        LOGGER.info("Attempt ${count}: Trying to initialize hikari pool...")
                    }
                    hikari = new HikariDataSource(config)
                    break

                } catch (Throwable e) {
                    Throwable connectEx = findConnectEx(e)
                    if (connectEx != null) {
                        LOGGER.error('Failed to initialize hikari pool! [Message: ' + connectEx.getMessage() + '] Retrying...')
                        if (count++ == retryCount) {
                            LOGGER.error('Retrying aborted!')
                            throw e
                        }
                        Thread.sleep(retryInterval)
                    } else {
                        throw e
                    }
                }
            }
        }
    }

    @CompileStatic
    private static Throwable findConnectEx(Throwable src) {
        if (src == null) {
            return null
        }

        if (src instanceof ConnectException) {
            return src
        } else {
            Throwable tmp = findConnectEx(src.cause)
            if (tmp != null) {
                return tmp
            }
        }
        null
    }

    @Override
    void shutdown() throws NyException {
        synchronized (hikariLock) {
            hikari.close()
        }
    }
}
