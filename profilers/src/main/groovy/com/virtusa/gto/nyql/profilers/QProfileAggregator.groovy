package com.virtusa.gto.nyql.profilers

import com.virtusa.gto.nyql.model.QProfiling
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QSession
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Aggregates several profiles into one profile instance.
 *
 * @author IWEERARATHNA
 */
class QProfileAggregator implements QProfiling {

    private static final Logger LOGGER = LoggerFactory.getLogger(QProfileAggregator)

    /**
     * list of all child profiles.
     */
    private final List<QProfiling> profilingList = [] as LinkedList

    private QProfileAggregator() {}

    /**
     * Creates a new aggregate profile instance having given profile set.
     *
     * @param profiles set of child profiles.
     * @return newly created aggregated profile.
     */
    public static QProfileAggregator create(QProfiling... profiles) {
        QProfileAggregator qProfileAggregator = new QProfileAggregator()
        qProfileAggregator.profilingList.addAll(Arrays.asList(profiles))
        return qProfileAggregator
    }

    @Override
    void start(Map options) {
        profilingList.each { it.start(options) }
    }

    @Override
    void doneParsing(final String scriptId, final long elapsed, final QSession session) {
        profilingList.each { it.doneParsing(scriptId, elapsed, session) }
    }

    @Override
    void doneExecuting(final QScript script, final long elapsed) {
        profilingList.each { it.doneExecuting(script, elapsed) }
    }

    @Override
    void close() throws IOException {
        profilingList.each { p -> safeClose({ p.close() }) }
    }

    private static void safeClose(Runnable runnable) {
        try {
            runnable.run()
        } catch (Throwable ignored) {
            // do nothing...
            LOGGER.error("Error closing child profiler!", ignored)
        }
    }
}
