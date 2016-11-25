package com.virtusa.gto.nyql.exceptions

import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class NySyntaxException extends NyException {

    NySyntaxException(String message) {
        super(message)
    }
}
