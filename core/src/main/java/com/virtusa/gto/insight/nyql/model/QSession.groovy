package com.virtusa.gto.insight.nyql.model

import com.virtusa.gto.insight.nyql.DSL
import com.virtusa.gto.insight.nyql.DSLContext
import com.virtusa.gto.insight.nyql.QExecutor
import com.virtusa.gto.insight.nyql.utils.Constants

import java.util.concurrent.ConcurrentHashMap

/**
 * @author IWEERARATHNA
 */
class QSession {

    Map<String, Object> sessionVariables = [:] as ConcurrentHashMap

    QRepository scriptRepo

    QExecutorFactory executorFactory
    QExecutor executor

    DSLContext dslContext

    private QSession() {}

    static QSession create() {
        return create(DSLContext.getActiveDSLContext(),
                QRepositoryRegistry.instance.defaultRepository(),
                null,
                QExecutorRegistry.instance.defaultExecutorFactory())
    }

    static QSession create(DSLContext context, QRepository repository, QExecutor executor, QExecutorFactory executorFactory) {
        QSession session = new QSession()

        session.dslContext = context
        session.scriptRepo = repository
        session.executor = executor
        session.executorFactory = executorFactory
        session.sessionVariables[Constants.DSL_ENTRY_WORD] = new DSL(session)
        session.sessionVariables[Constants.DSL_SESSION_WORD] = session.sessionVariables
        return session
    }
    
}
