package com.virtusa.gto.nyql.exceptions

/**
 * @author iweerarathna
 */
class NyRuntimeException extends RuntimeException {

    NyRuntimeException(String var1) {
        super(var1)
    }

    NyRuntimeException(String var1, Throwable var2) {
        super(var1, var2)
    }
}
