package com.virtusa.gto.insight.nyql.model

/**
 * @author IWEERARATHNA
 */
interface QProfiling extends Closeable {

    void start(Map options)

    void doneParsing(String scriptId, long elapsed, QSession session)

    void doneExecuting(QScript script, long elapsed)

}