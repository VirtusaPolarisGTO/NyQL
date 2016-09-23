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
        return proxy == null ? "" : proxy.query ?: ""
    }
}
