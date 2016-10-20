package com.virtusa.gto.insight.nyql.model.units

/**
 * @author IWEERARATHNA
 */
class QString {

    String text
    String __alias

    def alias(String theAlias) {
        __alias = theAlias
        return this
    }

    boolean __aliasDefined() {
        return __alias != null && !__alias.isEmpty()
    }
}