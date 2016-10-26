package com.virtusa.gto.nyql.model.impl

import com.virtusa.gto.nyql.model.QProfiling
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QSession
import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class QNoProfiling implements QProfiling {

    public static final QNoProfiling INSTANCE = new QNoProfiling()

    private QNoProfiling() { }

    @Override
    void start(Map options) {
        // do nothing
    }

    @Override
    void doneParsing(String scriptId, long elapsed, QSession session) {
        // do nothing
    }

    @Override
    void doneExecuting(QScript script, long elapsed) {
        // do nothing
    }

    @Override
    void close() throws IOException {
        // do nothing
    }
}
