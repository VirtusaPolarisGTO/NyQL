package com.virtusa.gto.insight.nyql.engine.impl

import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.QExecutor
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.utils.QUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author IWEERARATHNA
 */
class QDummyExecutor implements QExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(QDummyExecutor)

    @Override
    def execute(QScript script) throws Exception {
        LOGGER.debug("=====================================================================")
        LOGGER.debug("Executing query:")
        LOGGER.debug("\t${script.proxy.query.trim()}")
        if (QUtils.notNullNorEmpty(script.proxy.orderedParameters)) {
            LOGGER.debug("-------------------------------------------------")
            LOGGER.debug("  with ")
            script.proxy.orderedParameters.each {LOGGER.debug("    $it")}
        }

        int p = new Random(System.currentTimeMillis()).nextInt(10)
        if (p % 2 == 0) {
            LOGGER.debug("  Returning list")
            return ["isuru", "wee"]
        } else {
            LOGGER.debug("  Returning numeric")
            return p
        }
    }

    @Override
    void startTransaction() throws NyException {
        LOGGER.debug("Starting a new transaction...")
    }

    @Override
    void commit() throws NyException {
        LOGGER.debug("Committed.")
    }

    @Override
    def checkPoint() throws NyException {
        LOGGER.debug("Adding checkpoint here. . .")
        return null
    }

    @Override
    void rollback(Object checkpoint) throws NyException {
        LOGGER.debug("Rollback to the checkpoint: " + checkpoint ?: "<>")
    }

    @Override
    void done() throws NyException {
        LOGGER.debug("Script is done!")
    }

    @Override
    void close() throws IOException {
        LOGGER.debug("Closing executor!")
    }
}
