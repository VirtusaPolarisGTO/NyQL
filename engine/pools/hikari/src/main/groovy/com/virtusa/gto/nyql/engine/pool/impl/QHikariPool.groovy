package com.virtusa.gto.nyql.engine.pool.impl

import com.virtusa.gto.nyql.configs.ConfigKeys
import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.configs.ConfigurationsV2
import com.virtusa.gto.nyql.engine.pool.QJdbcPool
import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.exceptions.NyInitializationException
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

    private static final String DEF_POOL_SUFFIX = '-Pool'

    private static final Logger LOGGER =  LoggerFactory.getLogger(QHikariPool)
    private HikariDataSource hikari = null
    private final Object hikariLock = new Object()

    @Override
    Connection getConnection() throws NyException {
        LOGGER.trace('Requesting a new connection')
        hikari.getConnection()
    }

    @Override
    String getName() {
        'hikari'
    }

    @Override
    void init(Map options, Configurations configurations) throws NyException {
        HikariConfig config = new HikariConfig();
        //config.setDataSourceClassName(String.valueOf(options.dataSourceClassName))
        String jdbcDriverClass = QUtils.readEnv(ConfigKeys.SYS_JDBC_DRIVER)
        String jdbcUrl = QUtils.readEnv(ConfigKeys.SYS_JDBC_URL, String.valueOf(options.url))
        String jdbcUserName = QUtils.readEnv(ConfigKeys.SYS_JDBC_USERNAME, String.valueOf(options.username))

        if (jdbcDriverClass != null) {
            config.setDriverClassName(jdbcDriverClass)
        } else if (options.containsKey(ConfigKeys.JDBC_DRIVER_CLASS_KEY)) {
            config.setDriverClassName(String.valueOf(options.jdbcDriverClass))
        }
        config.setJdbcUrl(jdbcUrl)
        config.setUsername(jdbcUserName)
        config.setPassword(derivePassword(options, configurations))

        String poolName = configurations.getName() + DEF_POOL_SUFFIX
        config.setPoolName(poolName)

        boolean mbeans = configurations.isRegisterMXBeans()
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

            if (properties.hasProperty("registerMbeans")) {
                mbeans = properties.getOrDefault("registerMbeans", configurations.isRegisterMXBeans())
            }
        }

        // enable hikari beans
        LOGGER.info("Hikari mbeans registration: " + mbeans)
        config.setRegisterMbeans(mbeans)

        Integer retryCount = (Integer) options.getOrDefault('retryCount', 5)
        Integer retryInterval = (Integer) options.getOrDefault('retryInterval', 5000)
        initHikariPool(config, retryCount, retryInterval)
    }

    @CompileStatic
    private static String derivePassword(Map options, Configurations configurations) throws NyConfigurationException {
        String passEnc = QUtils.readEnv(ConfigKeys.SYS_JDBC_PASSWORD_ENC)
        String jdbcPassword = QUtils.readEnv(ConfigKeys.SYS_JDBC_PASSWORD)
        boolean isV2 = configurations instanceof ConfigurationsV2

        try {
            if (passEnc != null) {
                return new String(Base64.decoder.decode(passEnc), StandardCharsets.UTF_8)
            } else if (jdbcPassword != null) {
                return jdbcPassword
            } else if (options.passwordEnc) {
                return new String(Base64.decoder.decode(String.valueOf(options.passwordEnc)), StandardCharsets.UTF_8)
            } else {
                if (isV2) {
                    // in version 2, it is mandatory to specify password base64 encoded
                    return new String(Base64.decoder.decode(String.valueOf(options.password)), StandardCharsets.UTF_8)
                } else {
                    return String.valueOf(options.password)
                }
            }
        } catch (Exception ex) {
            if (isV2) {
                throw new NyConfigurationException('In NyQL v2, it is mandatory to specify password in base64 encoded format!', ex)
            } else {
                throw new NyConfigurationException('Failed to decode password as given in configuration!', ex)
            }
        }
    }

    @CompileStatic
    private void initHikariPool(HikariConfig config, int defRetryCount, int defRetryInterval) {
        int retryInterval = Integer.parseInt(QUtils.readEnv(ConfigKeys.SYS_CONNECT_RETRY_INTERVAL, String.valueOf(defRetryInterval)))
        int retryCount = Integer.parseInt(QUtils.readEnv(ConfigKeys.SYS_CONNECT_RETRY_COUNT, String.valueOf(defRetryCount)))

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
                            throw new NyInitializationException('Failed to initialize hikari connection pool!', e)
                        }
                        Thread.sleep(retryInterval)
                    } else {
                        throw new NyInitializationException('Failed to initialize hikari connection pool!', e)
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
