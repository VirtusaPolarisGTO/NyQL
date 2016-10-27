package com.virtusa.gto.nyql.model

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * @author IWEERARATHNA
 */
@CompileStatic
@Immutable
class QSource {

    private String id
    private File file
    private GroovyCodeSource codeSource

    String getId() {
        id
    }

    File getFile() {
        file
    }

    GroovyCodeSource getCodeSource() {
        codeSource
    }
}
