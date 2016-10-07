package com.virtusa.gto.insight.nyql.model

/**
 * @author IWEERARATHNA
 */
interface QProfiling extends Closeable {

    void doneParsing(String scriptId, long elapsed, QSession session)

    void doneExecuting(QScript script, long elapsed)

}