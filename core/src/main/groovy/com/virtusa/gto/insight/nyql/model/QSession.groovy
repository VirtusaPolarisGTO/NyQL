package com.virtusa.gto.insight.nyql.model

import com.virtusa.gto.insight.nyql.DSL
import com.virtusa.gto.insight.nyql.DSLContext
import com.virtusa.gto.insight.nyql.utils.Constants
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author IWEERARATHNA
 */
class QSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(QSession.class)

    String rootScriptId
    final Stack<String> scriptStack = new Stack<>()
    private final Object stackLock = new Object()
    Map<String, Object> sessionVariables = Collections.synchronizedMap([:])

    QRepository scriptRepo

    QExecutorFactory executorFactory
    QExecutor executor

    DSLContext dslContext

    private int execDepth = 0
    private final Object depthLock = new Object()

    private QSession() {}

    static QSession create(String theScriptId) {
        QSession qSession = createSession(DSLContext.getActiveDSLContext(),
                QRepositoryRegistry.instance.defaultRepository(),
                null,
                QExecutorRegistry.instance.defaultExecutorFactory())
        qSession.rootScriptId = theScriptId
        qSession.scriptStack.push(theScriptId)
        return qSession
    }

    static QSession createSession(DSLContext context, QRepository repository, QExecutor executor, QExecutorFactory executorFactory) {
        QSession session = new QSession()

        session.dslContext = context
        session.scriptRepo = repository
        session.executor = executor
        session.executorFactory = executorFactory
        session.sessionVariables[Constants.DSL_ENTRY_WORD] = new DSL(session)
        session.sessionVariables[Constants.DSL_SESSION_WORD] = session.sessionVariables
        return session
    }

    void intoScript(String scriptId) {
        synchronized (stackLock) {
            scriptStack.push(scriptId)
        }
    }

    void outFromScript(String scriptId) {
        synchronized (stackLock) {
            scriptStack.pop()
        }
    }

    String currentActiveScript() {
        synchronized (stackLock) {
            scriptStack.peek()
        }
    }

    QExecutor beingScript() {
        executor = executorFactory.createReusable();
        def stack = incrStack()
        LOGGER.debug("Session {} starting script at execution depth {}", this, stack)
        return executor
    }

    void closeScript() {
        def stack = decrStack()
        if (executor != null && stack <= 0) {
            LOGGER.debug("Closing executor since script has completed running.")
            executor.close()
        } else if (stack > 0) {
            LOGGER.debug("Session {} ended script at execution depth {}", this, stack)
        }
    }

    def execute(QScript script) {
        if (executor != null) {
            return executor.execute(script)
        } else {
            return executorFactory.create().execute(script)
        }
    }

    def execute(QScriptList scriptList) {
        if (executor != null) {
            return executor.execute(scriptList)
        } else {
            return executorFactory.create().execute(scriptList)
        }
    }

    private int incrStack() {
        synchronized (depthLock) {
            ++execDepth
        }
    }

    private int decrStack() {
        synchronized (depthLock) {
            --execDepth
        }
    }


    @Override
    public String toString() {
        return 'QSession@' + Integer.toHexString(hashCode());
    }
}
