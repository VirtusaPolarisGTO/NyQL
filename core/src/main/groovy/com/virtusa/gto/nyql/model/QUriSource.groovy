package com.virtusa.gto.nyql.model

import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class QUriSource extends QSource {

    private URI uri

    QUriSource(String theId, URI theUrl, GroovyCodeSource theCode) {
        super(theId, theCode)

        uri = theUrl
    }

    @Override
    NyBaseScript parseIn(GroovyShell shell) {
        shell.parse(uri) as NyBaseScript
    }
}
