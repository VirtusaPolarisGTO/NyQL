package com.virtusa.gto.insight.nyql.model.units

import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class QNumber {

    Number number
    String __alias

    def alias(String theAlias) {
        __alias = theAlias
        this
    }

    boolean __aliasDefined() {
        __alias != null && !__alias.isEmpty()
    }

}
