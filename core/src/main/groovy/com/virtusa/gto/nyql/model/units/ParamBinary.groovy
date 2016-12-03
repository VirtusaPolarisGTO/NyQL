package com.virtusa.gto.nyql.model.units

import groovy.transform.CompileStatic

/**
 * @author Isuru Weerarathna
 */
@CompileStatic
class ParamBinary extends AParam {

    @Override
    boolean __shouldValueConvert() {
        true
    }
}
