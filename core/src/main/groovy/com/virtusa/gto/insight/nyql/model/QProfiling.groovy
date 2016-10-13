package com.virtusa.gto.insight.nyql.model

/**
 * Base profile interface for any kind of profiling implementation.
 *
 * @author IWEERARATHNA
 */
interface QProfiling extends Closeable {

    /**
     * Start the profiler. This method will be called before any of parsing
     * or executing method is being called.
     *
     * @param options options for profiler initialization.
     */
    void start(Map options)

    /**
     * Called when a parsing is completed.
     *
     * @param scriptId script id.
     * @param elapsed time took to complete parsing.
     * @param session session instance used for parsing.
     */
    void doneParsing(String scriptId, long elapsed, QSession session)

    /**
     * Called when an execution is completed.
     *
     * @param script script executed.
     * @param elapsed time took to complete executing.
     */
    void doneExecuting(QScript script, long elapsed)

}