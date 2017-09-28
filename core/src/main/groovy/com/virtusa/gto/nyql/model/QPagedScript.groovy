package com.virtusa.gto.nyql.model

import groovy.transform.CompileStatic

/**
 * @author iweerarathna
 */
@CompileStatic
class QPagedScript extends QScript {

    int pageSize

    QPagedScript(QScript script, int pageSize) {
        super.id = script.id
        super.proxy = script.proxy
        super.qSession = script.qSession

        this.pageSize = pageSize
    }

}
