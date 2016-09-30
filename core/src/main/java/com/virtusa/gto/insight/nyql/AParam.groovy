package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.utils.QUtils
import groovy.transform.ToString

/**
 * @author IWEERARATHNA
 */
@ToString(includePackage = false)
class AParam {

    String __name
    String __alias

    def type

    int length = 0

    ParamScope scope

    String __mappingParamName

    AParam alias(String theAlias) {
        __alias = theAlias
        return this
    }

    boolean __aliasDefined() {
        return __alias != null && !__alias.isEmpty()
    }

    static enum ParamScope {
        IN, OUT, INOUT
    }

}
