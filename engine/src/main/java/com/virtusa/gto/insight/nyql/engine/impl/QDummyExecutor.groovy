package com.virtusa.gto.insight.nyql.engine.impl

import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.utils.QUtils
import com.virtusa.gto.insight.nyql.model.QExecutor
import com.virtusa.gto.insight.nyql.model.QScript

/**
 * @author IWEERARATHNA
 */
class QDummyExecutor implements QExecutor {

    @Override
    def execute(QScript script) throws Exception {
        println "====================================================================="
        println "Executing query:"
        println "\t${script.proxy.query.trim()}"
        if (QUtils.notNullNorEmpty(script.proxy.orderedParameters)) {
            println "-------------------------------------------------"
            println("  with ")
            script.proxy.orderedParameters.each {println("    $it")}
        }

        int p = new Random(System.currentTimeMillis()).nextInt(10)
        if (p % 2 == 0) {
            println "  Returning list"
            return ["isuru", "wee"]
        } else {
            println "  Returning numeric"
            return p
        }
    }

    @Override
    void startTransaction() throws NyException {
        println "Starting a new transaction..."
    }

    @Override
    void commit() throws NyException {
        println("Committed.")
    }

    @Override
    def checkPoint() throws NyException {
        println("Adding checkpoint here. . .")
        return null
    }

    @Override
    void rollback(Object checkpoint) throws NyException {
        println "Rollback to the checkpoint: " + checkpoint ?: "<>"
    }

    @Override
    void done() throws NyException {
        println "Script is done!"
    }

    @Override
    void close() throws IOException {
        println("Closing executor!")
    }
}
