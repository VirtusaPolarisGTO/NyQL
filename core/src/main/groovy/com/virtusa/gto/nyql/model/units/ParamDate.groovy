package com.virtusa.gto.nyql.model.units

import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class ParamDate extends AParam {

    @Override
    boolean __shouldValueConvert() {
        true
    }
}
