package com.virtusa.gto.nyql.model.units

import groovy.transform.CompileStatic
/**
 * @author IWEERARATHNA
 */
@CompileStatic
class ParamTimestamp extends AParam {

    String __tsFormat

    @Override
    boolean __shouldValueConvert() {
        true
    }
}
