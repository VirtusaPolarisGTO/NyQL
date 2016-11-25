package com.virtusa.gto.nyql.exceptions

import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class NyScriptNotFoundException extends NyException {

    NyScriptNotFoundException(String scriptId) {
        super("A script mapping has not been found for script '$scriptId'!")
    }

}
