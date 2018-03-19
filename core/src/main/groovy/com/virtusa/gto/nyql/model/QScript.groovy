package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.QResultProxy
import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * @author IWEERARATHNA
 */
@CompileStatic
@ToString(excludes = ["qSession"])
class QScript implements Serializable {

    String id
    QResultProxy proxy
    QSession qSession

    QScript spawn() {
        QScript script = new QScript(id: id, qSession: (QSession)null)
        QResultProxy resultProxy = proxy
        if (resultProxy != null) {
            script.proxy = resultProxy.dehydrate()
        }
        script
    }

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
