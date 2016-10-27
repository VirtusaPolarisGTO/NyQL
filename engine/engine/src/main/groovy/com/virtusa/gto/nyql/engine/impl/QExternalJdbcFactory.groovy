package com.virtusa.gto.nyql.engine.impl

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

    QExternalJdbcFactory(Connection connection) {
        this.connection = connection
    }

    @Override
    void init(Map options) {

    }

    @Override
    QExecutor create() {
        new QJdbcExecutor(connection)
    }

    @Override
    QExecutor createReusable() {
        new QJdbcExecutor(connection)
    }

    @Override
    void shutdown() {
        if (connection != null && !connection.isClosed()) {
            connection.close()
        }
    }
}
