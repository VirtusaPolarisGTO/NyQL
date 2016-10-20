package com.virtusa.gto.insight.nyql.exceptions

import com.virtusa.gto.insight.nyql.exceptions.NyException
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
