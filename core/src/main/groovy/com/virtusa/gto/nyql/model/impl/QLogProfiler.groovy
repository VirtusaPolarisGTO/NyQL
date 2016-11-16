package com.virtusa.gto.nyql.model.impl

import com.virtusa.gto.nyql.model.QProfiling
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QSession
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class QLogProfiler implements QProfiling {

    static final QLogProfiler INSTANCE = new QLogProfiler()

    private static final Logger LOGGER = LoggerFactory.getLogger(QLogProfiler)
    private static final String TAG = '[NyPROFILE]'
    private static final String TOOK = ' took ('
    private static final String TIME = ' ms) time.'

    /**
     * Default logging level for profiles: INFO
     *    0 = INFO
     *    1 = DEBUG
     *    2 = TRACE
     */
    private int defaultLevel = 0

    private QLogProfiler() {}

    @Override
    void start(Map options) {
        String level = (options['level'] ?: 'info').toString().toLowerCase()
        LOGGER.info(TAG + ' Log profiler started. Logging level: ' + level)

        if (level == 'debug') {
            defaultLevel = 1
        } else if (level == 'trace') {
            defaultLevel = 2
        } else {
            defaultLevel = 0
        }
    }

    @Override
    void doneParsing(String scriptId, long elapsed, QSession session) {
        String msg = TAG + ' Script parsing ' + scriptId + TOOK + elapsed + TIME
        if (defaultLevel < 1) {
            LOGGER.info(msg)
        } else if (defaultLevel == 1) {
            LOGGER.debug(msg)
        } else {
            LOGGER.trace(msg)
        }
    }

    @Override
    void doneExecuting(QScript script, long elapsed) {
        String msg = TAG + ' Script execution ' + script.id + TOOK + elapsed + TIME
        if (defaultLevel < 1) {
            LOGGER.info(msg)
        } else if (defaultLevel == 1) {
            LOGGER.debug(msg)
        } else {
            LOGGER.trace(msg)
        }
    }

    @Override
    void close() throws IOException {
        LOGGER.info('Closing log profile.')
    }
}
