package com.virtusa.gto.nyql.exceptions

/**
 * @author IWEERARATHNA
 */
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
