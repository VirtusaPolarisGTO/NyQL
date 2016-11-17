package com.virtusa.gto.nyql.model.units

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * @author IWEERARATHNA
 */
@CompileStatic
@ToString(includePackage = false)
class AParam {

    String __name
    String __alias

    AParam alias(String theAlias) {
        __alias = theAlias
        this
    }

    boolean __aliasDefined() {
        __alias != null && !__alias.isEmpty()
    }

    boolean __shouldValueConvert() {
        false
    }

    static enum ParamScope {
        IN, OUT, INOUT
    }

}
