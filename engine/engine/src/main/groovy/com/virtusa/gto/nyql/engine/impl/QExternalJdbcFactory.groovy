package com.virtusa.gto.nyql.engine.impl

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.model.DbInfo
import com.virtusa.gto.nyql.model.QExecutor
import com.virtusa.gto.nyql.model.QExecutorFactory

@java.lang.SuppressWarnings('JdbcConnectionReference')
import java.sql.Connection

/**
 * Executor factory class which being used to execute using external
 * jdbc connection rather than getting it pool. The connection should
 * be provided by the application.
 *
 * @author IWEERARATHNA
 */
class QExternalJdbcFactory implements QExecutorFactory {

    private final Connection connection
    private Configurations nyqlConfigs

    QExternalJdbcFactory(Connection connection) {
        this.connection = connection
    }

    @Override
    String getName() {
        'jdbc-external'
    }

    @Override
    DbInfo init(Map options, Configurations configurations) {
        nyqlConfigs = configurations
        DbInfo.deriveFromConnection(connection)
    }

    /**
     * By default this creates a reusable jdbc executor.
     *
     * @return executor instance.
     */
    @Override
    QExecutor create() {
        new QJdbcExecutor(connection, nyqlConfigs)
    }

    /**
     * By default, this also creates a reusable jdbc executor.
     *
     * @return executor instance.
     */
    @Override
    QExecutor createReusable() {
        new QJdbcExecutor(connection, nyqlConfigs)
    }

    @Override
    void shutdown() {
        if (connection != null && !connection.isClosed()) {
            connection.close()
        }
    }
}
