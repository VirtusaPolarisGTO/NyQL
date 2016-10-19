package com.virtusa.gto.insight.nyql.engine.impl

import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.QExecutor
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QScriptResult
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
        if (script instanceof QScriptResult) {
            return script.scriptResult
        }
        LOGGER.debug('=====================================================================')
        LOGGER.debug('Executing query:')
        LOGGER.debug("\t${script.proxy.query.trim()}")
        if (QUtils.notNullNorEmpty(script.proxy.orderedParameters)) {
            LOGGER.debug('-------------------------------------------------')
            LOGGER.debug('  with ')
            script.proxy.orderedParameters.each {LOGGER.debug("    $it")}
        }

        LOGGER.debug('  Returning list of maps')
        [[id: "1", title: "item-1"]]
    }

    @Override
    void startTransaction() throws NyException {
        LOGGER.debug('Starting a new transaction...')
    }

    @Override
    void commit() throws NyException {
        LOGGER.debug('Committed.')
    }

    @Override
    def checkPoint() throws NyException {
        LOGGER.debug('Adding checkpoint here. . .')
        return new Object()
    }

    @Override
    void rollback(Object checkpoint) throws NyException {
        LOGGER.debug('Rollback to the checkpoint: ' + checkpoint ?: '<>')
    }

    @Override
    void done() throws NyException {
        LOGGER.debug('Transaction is done!')
    }

    @Override
    void close() throws IOException {
        LOGGER.debug('Closing executor!')
    }
}
