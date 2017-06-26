package com.virtusa.gto.nyql.engine.pool.impl

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.virtusa.gto.nyql.configs.ConfigKeys
import com.virtusa.gto.nyql.engine.pool.QJdbcPool
import com.virtusa.gto.nyql.exceptions.NyException
import groovy.transform.CompileStatic

import java.sql.Connection
/**
 * @author IWEERARATHNA
 */
@CompileStatic
class QC3p0Pool implements QJdbcPool {

    private ComboPooledDataSource pool = new ComboPooledDataSource()

    @Override
    void init(Map options) throws NyException {
        if (options.containsKey(ConfigKeys.JDBC_DRIVER_CLASS_KEY)) {
            pool.setDriverClass(String.valueOf(options.jdbcDriverClass))
        }
        pool.setJdbcUrl(String.valueOf(options.url))
        pool.setUser(String.valueOf(options.username))
        pool.setPassword(String.valueOf(options.password))

        if (options.pooling) {
            Map poolingConfigs = options.pooling as Map
            Properties properties = new Properties()

            poolingConfigs.each { k, v ->
                if (pool.hasProperty(String.valueOf(k))) {
                    properties.put(String.valueOf(k), v)
                }
            }
            pool.setProperties(properties)
        }
    }

    @Override
    Connection getConnection() throws NyException {
        pool.getConnection()
    }

    @Override
    void shutdown() throws NyException {
        pool.close()
    }
}
