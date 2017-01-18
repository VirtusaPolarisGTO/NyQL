package com.virtusa.gto.nyql.exceptions

/**
 * Exception thrown when any initialization failure occurred while start-up.
 *
 * @author iweerarathna
 */
class NyInitializationException extends NyException {

    NyInitializationException(String message, Throwable cause) {
        super(message, cause)
    }
}
