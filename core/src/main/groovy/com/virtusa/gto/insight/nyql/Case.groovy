package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.traits.DataTypeTraits

/**
 * @author IWEERARATHNA
 */
class Case extends Column implements DataTypeTraits {

    QContext _ctx = null
    Query _ownerQ = null

    CaseType caseType = CaseType.OTHER
    private List<CaseCondition> allConditions = new ArrayList<>()
    private def __else = null
    private Where __lastWhere = null

    def getElse() {
        return __else
    }

    List<CaseCondition> getAllConditions() {
        return allConditions
    }

    def ELSE(closure) {
        def code = closure.rehydrate(_ownerQ ?: this, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        def result = code()

        __else = result
        return this
    }

    def THEN(closure) {
        if (__lastWhere == null) {
            throw new RuntimeException("No associated WHEN condition found for this THEN!")
        }

        def code = closure.rehydrate(_ownerQ ?: this, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        def result = code()

        allConditions.add(new CaseCondition(_theCondition: __lastWhere, _theResult: result))
        return this
    }

    def WHEN(closure) {
        Where whr = new Where(_ctx)

        def code = closure.rehydrate(whr, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        __lastWhere = whr
        return this
    }

    static class CaseCondition {
        Where _theCondition
        def _theResult
    }

    static enum CaseType {
        IFNULL,
        OTHER
    }
}

