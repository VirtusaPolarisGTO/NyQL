package com.virtusa.gto.nyql.engine.impl
@java.lang.SuppressWarnings('JdbcConnectionReference')
import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.model.DbInfo
import com.virtusa.gto.nyql.model.QExecutor
import com.virtusa.gto.nyql.model.QExecutorFactory
import groovy.transform.CompileStatic

import javax.naming.InitialContext
import javax.sql.DataSource
import java.sql.SQLException
/**
 * Executor factory class which being used to execute using external
 * {@link javax.sql.DataSource} rather than instantiating its own data source.
 * The external data source will be fetched using JNDI name.
 *
 * @author IWEERARATHNA
 */
@CompileStatic
class QJndiDataSourceFactory implements QExecutorFactory {

    private Configurations nyqlConfigs
    private DataSource dataSource

    QJndiDataSourceFactory() {
    }

    @Override
    String getName() {
        'jndi'
    }

    @Override
    DbInfo init(Map options, Configurations configurations) {
        def jndiName = (String) options.get('jndiName')
        if (jndiName == null || jndiName.isEmpty()) {
            throw new NyConfigurationException("Mandatory parameter 'jndiName' is missing for 'jndi' executor!")
        }

        try {
            dataSource = (DataSource) new InitialContext().lookup(jndiName)
        } catch (Exception ex) {
            throw new NyConfigurationException("Failed to lookup datasource from '${jndiName}': ${ex.getMessage()}", ex)
        }
        nyqlConfigs = configurations

        def connection = null
        try {
            connection = dataSource.getConnection()
            DbInfo.deriveFromConnection(connection)
        } catch (SQLException ex) {
            throw new NyConfigurationException('Failed to initialize database connection from datasource!', ex)
        } finally {
            if (connection != null) {
                connection.close()
            }
        }
    }


    /**
     * By default this creates a reusable jdbc executor.
     *
     * @return executor instance.
     */
    @Override
    QExecutor create() {
        new QJdbcExecutor(dataSource.getConnection(), nyqlConfigs)
    }

    /**
     * By default, this also creates a reusable jdbc executor.
     *
     * @return executor instance.
     */
    @Override
    QExecutor createReusable() {
        new QJdbcExecutor(dataSource.getConnection(), nyqlConfigs)
    }

    @Override
    void shutdown() {
        dataSource = null
    }
}
