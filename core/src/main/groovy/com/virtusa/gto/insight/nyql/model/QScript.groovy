package com.virtusa.gto.insight.nyql.model

import com.virtusa.gto.insight.nyql.QResultProxy
import groovy.transform.ToString

/**
 * @author IWEERARATHNA
 */
@ToString(excludes = ["qSession"])
class QScript {

    String id
    QResultProxy proxy
    QSession qSession

    void free() {
        proxy.free()
        qSession.free()
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
