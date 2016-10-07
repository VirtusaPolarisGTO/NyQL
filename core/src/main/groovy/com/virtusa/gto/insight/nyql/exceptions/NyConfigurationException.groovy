package com.virtusa.gto.insight.nyql.exceptions

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
