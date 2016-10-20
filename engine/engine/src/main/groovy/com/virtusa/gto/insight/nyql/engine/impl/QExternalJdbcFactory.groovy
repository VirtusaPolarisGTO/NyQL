package com.virtusa.gto.insight.nyql.engine.impl

import com.virtusa.gto.insight.nyql.model.QExecutor
import com.virtusa.gto.insight.nyql.model.QExecutorFactory

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
        return new QJdbcExecutor(connection)
    }

    @Override
    QExecutor createReusable() {
        return new QJdbcExecutor(connection)
    }

    @Override
    void shutdown() {
        if (connection != null && !connection.isClosed()) {
            connection.close()
        }
    }
}
