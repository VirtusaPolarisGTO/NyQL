package com.virtusa.gto.nyql.model.impl

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.QExecutor
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QScriptList
import com.virtusa.gto.nyql.model.QScriptResult
import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class QProfExecutor implements QExecutor {

    private final QExecutor executor
    private final Configurations configurations

    QProfExecutor(Configurations theConfigs, QExecutor qExecutor) {
        configurations = theConfigs
        executor = qExecutor
    }

    @Override
    def execute(QScriptList scriptList) throws Exception {
        executor.execute(scriptList)
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
        def result = null
        if (script instanceof QScriptList) {
            result = executor.execute((QScriptList)script)
        } else {
            result = executor.execute(script)
        }
        long e = System.currentTimeMillis()
        if (!(script instanceof QScriptResult)) {
            configurations.profiler.doneExecuting(script, (e - s))
        }
        result
    }

    @Override
    void close() throws IOException {
        executor.close()
    }
}
