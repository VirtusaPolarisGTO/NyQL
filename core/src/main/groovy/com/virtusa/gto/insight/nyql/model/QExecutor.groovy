package com.virtusa.gto.insight.nyql.model

import com.virtusa.gto.insight.nyql.exceptions.NyException

/**
 * @author IWEERARATHNA
 */
trait QExecutor implements Closeable {

    abstract void startTransaction() throws NyException

    abstract void commit() throws NyException

    abstract def checkPoint() throws NyException

    abstract void rollback(def checkpoint) throws NyException

    abstract void done() throws NyException

    def execute(QScriptList scriptList) throws Exception {
        if (scriptList == null || scriptList.scripts == null) {
            return null;
        }

        List results = []
        for (QScript qScript : scriptList.scripts) {
            def res = execute(qScript)
            results.add(res)
        }
        return results
    }

    abstract def execute(QScript script) throws Exception

}
