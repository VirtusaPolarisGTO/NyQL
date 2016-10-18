package com.virtusa.gto.insight.nyql.model

import groovy.transform.ToString

/**
 * @author IWEERARATHNA
 */
@ToString
class QScriptResult extends QScript {

    def scriptResult

    @Override
    void free() {
        super.free()
        scriptResult = null
    }
}
