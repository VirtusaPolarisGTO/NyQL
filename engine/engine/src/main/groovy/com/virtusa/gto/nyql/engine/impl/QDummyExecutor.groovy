package com.virtusa.gto.nyql.engine.impl

import com.virtusa.gto.nyql.UpsertQuery
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.QExecutor
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QScriptList
import com.virtusa.gto.nyql.model.QScriptListType
import com.virtusa.gto.nyql.model.QScriptResult
import com.virtusa.gto.nyql.utils.QReturnType
import com.virtusa.gto.nyql.utils.QUtils
import com.virtusa.gto.nyql.utils.QueryType
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

        if (script.proxy.queryType == QueryType.INSERT || script.proxy.queryType == QueryType.DELETE
                || script.proxy.queryType == QueryType.UPDATE) {
            if (script.proxy.returnType == QReturnType.KEYS) {
                new NyQLResult().appendCount(2, [10, 11])
            } else {
                new NyQLResult().appendCount(1)
            }
        } else if (script.proxy.queryType == QueryType.BULK_INSERT || script.proxy.queryType == QueryType.BULK_UPDATE) {
            int[] res = [1, 0, 1]
            new NyQLResult().appendCounts(res)
        } else {
            LOGGER.debug('  Returning list of maps')
            [[id: '1', title: 'item-1', 'aboolCol': true, price: 2.34, year: 2016],
             [id: '2', title: 'item-2', 'aboolCol': false, price: 1.0, year: '2016'],
             [id: '3', title: 'item-3', 'aboolCol': null, price: '34.5', year: null],
             [id: '4', title: 'item-4', 'aboolCol': 0, price: '0', year: '2017'],
             [id: '5', title: 'item-5', 'aboolCol': 1, price: null],
             [id: '6', title: 'item-6', 'aboolCol': 't', price: '-2.043243']] as NyQLResult
        }
    }

    @Override
    def execute(QScriptList scriptList) throws Exception {
        if (scriptList == null || scriptList.scripts == null) {
            return new LinkedList<>();
        }

        if (scriptList.type == QScriptListType.UPSERT) {
            if (scriptList.scripts.size() < 3) {
                throw new NyException('Not defined either select, insert, or update query in UPSERT query!')
            }

            UpsertQuery upsertQuery = (UpsertQuery)scriptList.baseQuery

            LOGGER.debug("Executing UPSERT:")
            LOGGER.debug("Executing 1st query to select existing records...")
            NyQLResult result = execute(scriptList.scripts[0]) as NyQLResult
            LOGGER.debug(">>>>")
            LOGGER.debug("If no results returned we execute this query:")
            // insert
            execute(scriptList.scripts[1])

            LOGGER.debug(">>>>")
            LOGGER.debug("If results returned, we execute this query:")
            // records exist... update
            execute(scriptList.scripts[2])

            if (upsertQuery.returningType == UpsertQuery.ReturnType.NONE) {
                LOGGER.debug("Upsert query has been defined not to return any result. So nothing will be executed.")
                return new NyQLResult()
            } else if (upsertQuery.returningType == UpsertQuery.ReturnType.RECORD_BEFORE) {
                LOGGER.debug("Upsert query will return result returned from 1st selection query.")
                return result
            } else {
                if (scriptList.scripts.size() < 4) {
                    throw new NyException('Query correspond to returning upsert result is not found!')
                } else {
                    LOGGER.debug(">>>")
                    LOGGER.debug("Upsert query will return updated result by executing this query:")
                    return execute(scriptList.scripts[3])
                }
            }

        } else if (scriptList.type == QScriptListType.INSERT_OR_LOAD) {
            LOGGER.debug("Executing INSERT Or LOAD query...")
            LOGGER.debug("Executing 1st query to select existing records...")
            NyQLResult result = execute(scriptList.scripts[0]) as NyQLResult

            LOGGER.debug(">>>>")
            LOGGER.debug("If no results returned we execute this query:")
            execute(scriptList.scripts[1])
            LOGGER.debug("Exeute 1st query to get the updated record and return it")
            result = execute(scriptList.scripts[0]) as NyQLResult // get the latest value

            LOGGER.debug(">>>>")
            LOGGER.debug("If results returned, we return result from 1st query.")
            return result
        } else {
            List results = []
            for (QScript qScript : scriptList.scripts) {
                def res = execute(qScript)
                results.add(res)
            }
            return results
        }
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
        new Object()
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
