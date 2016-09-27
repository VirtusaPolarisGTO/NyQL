package com.virtusa.gto.insight.nyql.model

import com.virtusa.gto.insight.nyql.QResultProxy
import groovy.transform.ToString

/**
 * @author IWEERARATHNA
 */
@ToString(excludes = ["qSession"])
class QScript {

    QResultProxy proxy
    QSession qSession

    @Override
    public String toString() {
        String params = "";
        if (proxy != null) {
            params = proxy.orderedParameters.toString()
        }
        return proxy == null ? "" : (proxy.query ?: "").trim() + "\n" + params
    }
}
