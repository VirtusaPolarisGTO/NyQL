package com.virtusa.gto.insight.nyql.model

import com.virtusa.gto.insight.nyql.QResultProxy
import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * @author IWEERARATHNA
 */
@CompileStatic
@ToString(excludes = ["qSession"])
class QScript {

    String id
    QResultProxy proxy
    QSession qSession

    void free() {
        if (proxy != null) {
            proxy.free()
        }
        if (qSession != null) {
            qSession.free()
        }
    }

    @Override
    public String toString() {
        String params = "";
        if (proxy != null) {
            params = proxy.orderedParameters.toString()
        }
        return proxy == null ? '' : (proxy.query ?: '').trim() + '\n' + params
    }
}
