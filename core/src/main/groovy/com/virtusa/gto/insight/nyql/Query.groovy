package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.utils.QReturnType

/**
 * @author Isuru Weerarathna
 */
class Query extends AbstractClause {

    QReturnType returnType = QReturnType.RESULT

    Where whereObj = null
    Table sourceTbl = null
    def _limit

    Query(QContext contextParam) {
        super(contextParam)
    }

    def IMPORT(String scriptId) {
        return $IMPORT(scriptId)
    }

    def LIMIT(Object total) {
        _limit = total
        return this
    }

    def WHERE(closure) {
        Where whr = new Where(_ctx)

        def code = closure.rehydrate(whr, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        whereObj = whr
        return this
    }



}