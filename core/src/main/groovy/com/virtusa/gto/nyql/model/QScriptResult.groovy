package com.virtusa.gto.nyql.model

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * @author IWEERARATHNA
 */
@CompileStatic
@ToString
class QScriptResult extends QScript {

    def scriptResult

    @Override
    void free() {
        super.free()
        scriptResult = null
    }
}
