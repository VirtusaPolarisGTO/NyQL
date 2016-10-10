package com.virtusa.gto.insight.nyql.profilers

import com.virtusa.gto.insight.nyql.model.QProfiling
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QSession
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.DecimalFormat
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

/**
 * Calculates average time for each query dynamically.
 *
 * @author IWEERARATHNA
 */
class QAverageProfiler implements QProfiling {

    private static final Logger LOGGER = LoggerFactory.getLogger(QAverageProfiler)

    private final Map<String, AQueryStat> statMap = [:] as ConcurrentHashMap
    private final Map<String, AQueryStat> parseStatMap = [:] as ConcurrentHashMap

    private static final DecimalFormat FORMATTER = new DecimalFormat("#0.0")

    @Override
    void start(Map options) {

    }

    @Override
    void doneParsing(String scriptId, long elapsed, QSession session) {
        AQueryStat queryStat = parseStatMap.computeIfAbsent(scriptId, { new AQueryStat() })
        queryStat.update(elapsed)
    }

    @Override
    void doneExecuting(QScript script, long elapsed) {
        AQueryStat queryStat = statMap.computeIfAbsent(script.id, { new AQueryStat() })
        queryStat.update(elapsed)
    }

    @Override
    void close() throws IOException {
        LOGGER.debug("-"*75)
        LOGGER.debug("Query Stats:")
        LOGGER.debug("-"*75)

        parseStatMap.each {
            LOGGER.debug("Query Parsing: " + it.key)
            LOGGER.debug("   > Invoked #:  " + it.value.getInvocationCount())
            LOGGER.debug("   > Avg Time :  " + FORMATTER.format(it.value.getAvgTime()) + " ms")
            LOGGER.debug("   > Max Time :  " + it.value.getMaxTime() + " ms")
            LOGGER.debug("   > Min Time :  " + it.value.getMinTime() + " ms")
        }

        statMap.each {
            LOGGER.debug("Query Execution: " + it.key)
            LOGGER.debug("   > Invoked #:  " + it.value.getInvocationCount())
            LOGGER.debug("   > Avg Time :  " + FORMATTER.format(it.value.getAvgTime()) + " ms")
            LOGGER.debug("   > Max Time :  " + it.value.getMaxTime() + " ms")
            LOGGER.debug("   > Min Time :  " + it.value.getMinTime() + " ms")
        }

        statMap.clear()
        parseStatMap.clear()
    }

    private static class AQueryStat {
        private long invocationCount = 0L
        private long totalTime = 0L
        private int maxTime = Integer.MIN_VALUE
        private int minTime = Integer.MAX_VALUE

        private final Object calcLock = new Object()

        private void update(long elapsed) {
            synchronized (calcLock) {
                invocationCount++
                totalTime += elapsed
                maxTime = Math.max(maxTime, elapsed)
                minTime = Math.min(minTime, elapsed)
            }
        }

        long getInvocationCount() {
            synchronized (calcLock) {
                return invocationCount
            }
        }

        double getAvgTime() {
            synchronized (calcLock) {
                if (invocationCount == 0) {
                    return 0.0
                } else {
                    totalTime / invocationCount
                }
            }
        }

        int getMaxTime() {
            synchronized (calcLock) {
                return maxTime
            }
        }

        int getMinTime() {
            synchronized (calcLock) {
                return minTime
            }
        }
    }
}
