package com.virtusa.gto.nyql.exceptions

import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class NyConfigurationException extends NyException {

    NyConfigurationException() {
    }

    NyConfigurationException(String message) {
        super(message)
    }

    NyConfigurationException(String message, Throwable cause) {
        super(message, cause)
    }
}
