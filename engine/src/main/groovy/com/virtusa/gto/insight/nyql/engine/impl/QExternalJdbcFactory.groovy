package com.virtusa.gto.insight.nyql.engine.impl

import com.virtusa.gto.insight.nyql.model.QExecutor
import com.virtusa.gto.insight.nyql.model.QExecutorFactory

import java.sql.Connection

/**
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
