package com.virtusa.gto.nyql.exceptions

import groovy.transform.InheritConstructors

/**
 * @author IWEERARATHNA
 */
@InheritConstructors
class NyScriptNotFoundException extends NyException {

    NyScriptNotFoundException(String scriptId) {
        super("A script mapping has not been found for script '$scriptId'!")
    }

}
