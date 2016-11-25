package com.virtusa.gto.nyql.exceptions

import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class NyException extends Exception {

    NyException() {
        super()
    }

    NyException(String message) {
        super(message)
    }

    NyException(String message, Throwable cause) {
        super(message, cause)
    }

    NyException(Throwable cause) {
        super(cause)
    }

    protected NyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace)
    }
}
