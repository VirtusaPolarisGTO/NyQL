package com.virtusa.gto.nyql.model

import groovy.transform.Immutable

/**
 * @author IWEERARATHNA
 */
@Immutable
class QSource {

    private String id
    private File file
    private boolean doCache = false
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
