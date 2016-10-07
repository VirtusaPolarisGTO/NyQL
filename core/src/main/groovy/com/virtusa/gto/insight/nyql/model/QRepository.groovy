package com.virtusa.gto.insight.nyql.model

import com.virtusa.gto.insight.nyql.QResultProxy
import com.virtusa.gto.insight.nyql.exceptions.NyException

/**
 * @author IWEERARATHNA
 */
trait QRepository implements Closeable {

    abstract void clearCache(int level)

    abstract QScript parse(String scriptId, QSession session) throws NyException

    QScript parse(QResultProxy resultProxy, QSession session = null) throws NyException {
        return new QScript(id: session?.currentActiveScript() + '@' + resultProxy.hashCode(), proxy: resultProxy, qSession: session)
    }

}