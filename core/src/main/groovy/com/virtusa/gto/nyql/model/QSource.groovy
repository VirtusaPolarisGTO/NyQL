package com.virtusa.gto.nyql.model

import groovy.transform.CompileStatic
/**
 * @author IWEERARATHNA
 */
@CompileStatic
class QSource {

    private final String id
    private final GroovyCodeSource codeSource

    QSource(String theId, GroovyCodeSource theCode) {
        id = theId
        codeSource = theCode
    }

    String getId() {
        id
    }

    GroovyCodeSource getCodeSource() {
        codeSource
    }

    boolean isValid() {
        codeSource != null
    }

    NyBaseScript parseIn(GroovyShell shell) {
        shell.parse(codeSource) as NyBaseScript
    }
}
