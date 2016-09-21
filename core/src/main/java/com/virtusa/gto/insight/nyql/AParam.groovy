package com.virtusa.gto.insight.nyql

import groovy.transform.ToString

/**
 * @author IWEERARATHNA
 */
@ToString(includePackage = false)
class AParam {

    String __name

    def type

    ParamScope scope

    String __mappingParamName

    static enum ParamScope {
        IN, OUT, INOUT
    }

}
