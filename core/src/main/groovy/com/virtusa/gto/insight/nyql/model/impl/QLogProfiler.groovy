package com.virtusa.gto.insight.nyql.model.impl

import com.virtusa.gto.insight.nyql.model.QProfiling
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QSession
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author IWEERARATHNA
 */
class QLogProfiler implements QProfiling {

    static final QLogProfiler INSTANCE = new QLogProfiler()

    private static final Logger LOGGER = LoggerFactory.getLogger(QLogProfiler)
    private static final String TAG = '[NyPROFILE]'

    private QLogProfiler() {}

    @Override
    void start(Map options) {
        LOGGER.info("$TAG Log profiler started.")
    }

    @Override
    void doneParsing(String scriptId, long elapsed, QSession session) {
        LOGGER.info("$TAG Script parsing '$scriptId' took ($elapsed ms) time.")
    }

    @Override
    void doneExecuting(QScript script, long elapsed) {
        LOGGER.info("$TAG Script execution '${script.id}' took ($elapsed ms) time.")
    }

    @Override
    void close() throws IOException {
        LOGGER.debug('Closing log profile.')
    }
}
