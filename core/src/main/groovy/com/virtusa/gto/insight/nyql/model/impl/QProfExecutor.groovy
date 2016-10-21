package com.virtusa.gto.insight.nyql.model.impl

import com.virtusa.gto.insight.nyql.configs.Configurations
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.QExecutor
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QScriptResult
import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class QProfExecutor implements QExecutor {

    private final QExecutor executor

    QProfExecutor(QExecutor qExecutor) {
        executor = qExecutor
    }

    @Override
    void startTransaction() throws NyException {
        executor.startTransaction()
    }

    @Override
    void commit() throws NyException {
        executor.commit()
    }

    @Override
    def checkPoint() throws NyException {
        executor.checkPoint()
    }

    @Override
    void rollback(Object checkpoint) throws NyException {
        executor.rollback(checkpoint)
    }

    @Override
    void done() throws NyException {
        executor.done()
    }

    @Override
    def execute(QScript script) throws Exception {
        long s = System.currentTimeMillis()
        def result = executor.execute(script)
        long e = System.currentTimeMillis()
        if (!(script instanceof QScriptResult)) {
            Configurations.instance().profiler.doneExecuting(script, (e - s))
        }
        result
    }

    @Override
    void close() throws IOException {
        executor.close()
    }
}
