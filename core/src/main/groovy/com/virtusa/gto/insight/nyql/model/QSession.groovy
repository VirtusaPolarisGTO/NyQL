package com.virtusa.gto.insight.nyql.model

import com.virtusa.gto.insight.nyql.DSL
import com.virtusa.gto.insight.nyql.DSLContext
import com.virtusa.gto.insight.nyql.db.QDbFactory
import com.virtusa.gto.insight.nyql.utils.Constants
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A context associated with a given script parsing and execution.
 *
 * @author IWEERARATHNA
 */
class QSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(QSession)

    /**
     * root script id which is the first (root) script which user has commanded to run.
     */
    String rootScriptId

    /**
     * Stack of scripts which are running.
     */
    final Stack<String> scriptStack = [] as Stack
    private final Object stackLock = new Object()

    /**
     * session variable set given by user.
     */
    Map<String, Object> sessionVariables = Collections.synchronizedMap([:])

    /**
     * active script repository.
     */
    QRepository scriptRepo

    /**
     * executor factory to execute scripts.
     */
    QExecutorFactory executorFactory

    /**
     * Database factory.
     */
    QDbFactory dbFactory

    /**
     * active executor for this session.
     */
    QExecutor executor

    /**
     * current execution depth.
     */
    private int execDepth = 0
    private final Object depthLock = new Object()

    private QSession() {}

    final void free() {
        synchronized (depthLock) {
            if (execDepth > 1) {
                LOGGER.warn('Cannot free session instance at this moment! Stack: ' + execDepth)
                return
            }
        }
        sessionVariables.clear()
        scriptStack.clear()
        scriptRepo = null
        executorFactory = null
        dbFactory = null
        executor = null
    }

    static QSession create(String theScriptId) {
        QSession qSession = createSession(DSLContext.getActiveDSLContext().activeFactory,
                QRepositoryRegistry.instance.defaultRepository(),
                null,
                QExecutorRegistry.instance.defaultExecutorFactory())
        qSession.rootScriptId = theScriptId
        qSession.scriptStack.push(theScriptId)
        return qSession
    }

    static QSession createSession(QDbFactory dbFactory, QRepository repository,
                                  QExecutor executor, QExecutorFactory executorFactory) {
        QSession session = new QSession()

        session.dbFactory = dbFactory
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
        if (executor == null) {
            executor = executorFactory.createReusable()
        }
        def stack = incrStack()
        LOGGER.debug('Session {} starting script at execution depth {}', this, stack)
        return executor
    }

    void closeScript() {
        def stack = decrStack()
        if (executor != null && stack <= 0) {
            LOGGER.debug('Closing executor since script has completed running.')
            executor.close()
            executor = null
        } else if (stack > 0) {
            LOGGER.debug('Session {} ended script at execution depth {}', this, stack)
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
    String toString() {
        return 'QSession@' + Integer.toHexString(hashCode())
    }
}
