package com.virtusa.gto.insight.nyql.profilers

import com.virtusa.gto.insight.nyql.model.QProfiling
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QSession
import groovy.json.JsonOutput
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Calculates average time for each query dynamically and emit them
 * to a file periodically.
 *
 * @author IWEERARATHNA
 */
class QAverageProfiler implements QProfiling {

    private static final Logger LOGGER = LoggerFactory.getLogger(QAverageProfiler)

    /**
     * map storing execution stats of scripts.
     */
    private final Map<String, AQueryStat> statMap = [:] as ConcurrentHashMap

    /**
     * map storing parsing stats of scripts.
     */
    private final Map<String, AQueryStat> parseStatMap = [:] as ConcurrentHashMap

    private static final DecimalFormat FORMATTER = new DecimalFormat("#0.0")

    /**
     * interval to save stats to a file.
     */
    private static final int DUMP_INTERVAL = 10000

    /**
     * output file for the stats.
     */
    private static final String OUTPUT_FILE = "./nyprofile-output.json"

    /**
     * thread pool used to save to the file.
     */
    private final ExecutorService POOL = Executors.newSingleThreadExecutor()

    /**
     * last dump time.
     */
    private long lastDumpTime = 0L
    private final Object timeLock = new Object()

    @Override
    void start(Map options) {
        synchronized (timeLock) {
            lastDumpTime = System.currentTimeMillis()
        }
    }

    @Override
    void doneParsing(String scriptId, long elapsed, QSession session) {
        AQueryStat queryStat = parseStatMap.computeIfAbsent(scriptId, { new AQueryStat() })
        queryStat.update(elapsed)

        // try to dump to the file...
        dumpToFile()
    }

    @Override
    void doneExecuting(QScript script, long elapsed) {
        AQueryStat queryStat = statMap.computeIfAbsent(script.id, { new AQueryStat() })
        queryStat.update(elapsed)

        // try to dump to the file...
        dumpToFile()
    }

    /**
     * Check whether interval has been exceeded, if so, save to the file.
     *
     * @param force flag indicating forcefully save to file or not.
     */
    private void dumpToFile(boolean force = false) {
        synchronized (timeLock) {
            long current = System.currentTimeMillis()
            boolean needWrite = force || DUMP_INTERVAL < (current - lastDumpTime)
            lastDumpTime = current

            if (needWrite) {
                POOL.execute({ writeToFile() })
            }
        }
    }

    /**
     * Write stats to the file.
     */
    private void writeToFile() {
        Map<String, Map> map = new HashMap<>()
        map.put("executions", statMap)
        map.put("parsing", parseStatMap)

        new File(OUTPUT_FILE).withWriter(StandardCharsets.UTF_8.name()) {
            it.write(JsonOutput.toJson(map))
            it.flush()
        }
    }

    @Override
    void close() throws IOException {
        dumpToLog()
        writeToFile()

        POOL.shutdown()
        statMap.clear()
        parseStatMap.clear()
    }

    /**
     * Dump all stats to the log.
     */
    private void dumpToLog() {
        LOGGER.debug("-"*75)
        LOGGER.debug("Query Stats:")
        LOGGER.debug("-"*75)

        parseStatMap.sort().each {
            AQueryStat execStat = statMap[it.key] ?: new AQueryStat()

            LOGGER.debug("Query: " + it.key)
            LOGGER.debug("  > Parsing  : {#: {}, Avg: {}, Max: {}, Min: {}}",
                            it.value.getInvocationCount(),
                            FORMATTER.format(it.value.getAvgTime()),
                            it.value.getMaxTime(),
                            it.value.getMinTime())

            LOGGER.debug("  > Execution: {#: {}, Avg: {} ms, Max: {} ms, Min: {} ms}",
                    execStat.getInvocationCount(),
                    FORMATTER.format(execStat.getAvgTime()),
                    execStat.getMaxTime(),
                    execStat.getMinTime())
        }

        LOGGER.debug("-"*75)
        statMap.sort().each {
            if (!parseStatMap.containsKey(it.key)) {
                LOGGER.debug("Query: " + it.key)
                LOGGER.debug("  > Execution  : {#: {}, Avg: {}, Max: {}, Min: {}}",
                        it.value.getInvocationCount(),
                        FORMATTER.format(it.value.getAvgTime()),
                        it.value.getMaxTime(),
                        it.value.getMinTime())
            }
        }
    }

    /**
     * A class storing stats for a single script.
     */
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

        /**
         * Returns number of invoked times of this script.
         *
         * @return invoked number.
         */
        long getInvocationCount() {
            synchronized (calcLock) {
                return invocationCount
            }
        }

        /**
         * Returns average time for query.
         *
         * @return average time for query.
         */
        double getAvgTime() {
            synchronized (calcLock) {
                if (invocationCount == 0) {
                    return 0.0
                } else {
                    totalTime / invocationCount
                }
            }
        }

        /**
         * Returns maximum time took for query.
         *
         * @return maximum time took for query.
         */
        int getMaxTime() {
            synchronized (calcLock) {
                return maxTime
            }
        }

        /**
         * Returns minimum time for query.
         *
         * @return minimum time for query.
         */
        int getMinTime() {
            synchronized (calcLock) {
                return minTime
            }
        }
    }
}
