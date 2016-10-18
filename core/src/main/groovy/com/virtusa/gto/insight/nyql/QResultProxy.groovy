package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.model.blocks.AParam
import com.virtusa.gto.insight.nyql.utils.QReturnType
import com.virtusa.gto.insight.nyql.utils.QueryType
import groovy.transform.ToString

/**
 * @author IWEERARATHNA
 */
@ToString(includePackage = false)
class QResultProxy {

    String query
    List<AParam> orderedParameters
    QueryType queryType = QueryType.UNKNOWN
    QReturnType returnType = QReturnType.RESULT

    def rawObject
    def qObject

    /**
     * Creates a new proxy result instance by removing session related entities.
     *
     * @return a new proxy instance cloned from this.
     */
    QResultProxy dehydrate() {
        QResultProxy proxy = new QResultProxy(query: this.query, queryType: this.queryType)
        List<AParam> paramList = [] as LinkedList
        if (orderedParameters != null) {
            paramList.addAll(orderedParameters)
        }
        proxy.orderedParameters = paramList
        proxy.rawObject = rawObject

        // we do not copy qObject since it has a session instance attached unless otherwise it is a part query...
        if (queryType == QueryType.PART && qObject != null && qObject instanceof QueryPart) {
            QContext cloned = qObject._ctx.cloneContext()
            QueryPart queryPart = new QueryPart(cloned)
            proxy.qObject = queryPart
        }
        proxy
    }

    final void free() {
        qObject = null
        rawObject = null
        orderedParameters.clear()
        query = null
    }
}
