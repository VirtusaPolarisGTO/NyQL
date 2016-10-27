package com.virtusa.gto.nyql

import com.virtusa.gto.nyql.exceptions.NySyntaxException
import com.virtusa.gto.nyql.traits.DataTypeTraits
import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
class Case extends Column implements DataTypeTraits {

    Query _ownerQ = null

    CaseType caseType = CaseType.OTHER
    private List<CaseCondition> allConditions = [] as Queue
    private __else = null
    private Where __lastWhere = null

    def getElse() {
        __else
    }

    List<CaseCondition> getAllConditions() {
        allConditions
    }

    Case ELSE(@DelegatesTo(value = Query, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        def code = closure.rehydrate(_ownerQ, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        def result = code()

        __else = result
        this
    }

    Case THEN(@DelegatesTo(value = Query, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        if (__lastWhere == null) {
            throw new NySyntaxException('No associated WHEN condition found for this THEN!')
        }

        def code = closure.rehydrate(_ownerQ, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        def result = code()

        allConditions.add(new CaseCondition(_theCondition: __lastWhere, _theResult: result))
        this
    }

    Case WHEN(@DelegatesTo(value = Where, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        Where whr = new Where(_ctx)

        def code = closure.rehydrate(whr, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        __lastWhere = whr
        this
    }

    @CompileStatic
    static class CaseCondition {
        Where _theCondition
        def _theResult
    }

    @CompileStatic
    static enum CaseType {
        IFNULL,
        OTHER
    }
}

