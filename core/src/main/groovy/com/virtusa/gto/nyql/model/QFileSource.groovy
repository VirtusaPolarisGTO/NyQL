package com.virtusa.gto.nyql.model

import groovy.transform.CompileStatic
/**
 * @author IWEERARATHNA
 */
@CompileStatic
class QFileSource extends QSource {

    private File file

    QFileSource(String id, File theFile, GroovyCodeSource codeSource) {
        super(id, codeSource)

        file = theFile
    }

    File getFile() {
        file
    }

    @Override
    NyBaseScript parseIn(GroovyShell shell) {
        shell.parse(file) as NyBaseScript
    }

    @Override
    boolean isValid() {
        file != null && file.exists()
    }
}
