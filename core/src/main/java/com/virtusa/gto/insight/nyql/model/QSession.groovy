package com.virtusa.gto.insight.nyql.model

import com.virtusa.gto.insight.nyql.DSL
import com.virtusa.gto.insight.nyql.DSLContext
import com.virtusa.gto.insight.nyql.QContext
import com.virtusa.gto.insight.nyql.QExecutor
import com.virtusa.gto.insight.nyql.utils.Constants

/**
 * @author IWEERARATHNA
 */
class QSession {

    Map<String, Object> sessionVariables = [:]

    Map<String, Object> paramValues = [:]

    QRepository scriptRepo

    QExecutor executor

    DSLContext dslContext

    private QSession() {}

    static QSession create() {
        return create(DSLContext.getActiveDSLContext(),
                QRepositoryRegistry.instance.defaultRepository(),
                QExecutorRegistry.instance.defaultExecutor())
    }

    static QSession create(DSLContext context, QRepository repository, QExecutor executor) {
        QSession session = new QSession()

        session.dslContext = context
        session.scriptRepo = repository
        session.executor = executor
        session.sessionVariables[Constants.DSL_ENTRY_WORD] = new DSL(session)
        session.sessionVariables[Constants.DSL_SESSION_WORD] = session.sessionVariables
        return session
    }

    QSession addSessionVariable(String name, Object value) {
        sessionVariables.put(name, value)
        return this
    }

    QSession addSessionVariables(Map<String, Object> sessionVars) {
        sessionVariables.putAll(sessionVars)
        return this
    }

    QSession addParameterValues(Map<String, Object> pValues) {
        paramValues.putAll(pValues)
        return this
    }

    QContext newQueryContext() {
        return new QContext(ownerSession: this, translator: dslContext.qTranslator, translatorName: dslContext.activeDb)
    }

}
