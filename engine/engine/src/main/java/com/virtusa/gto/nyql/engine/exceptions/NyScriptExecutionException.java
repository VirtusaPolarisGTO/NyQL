package com.virtusa.gto.nyql.engine.exceptions;

import com.virtusa.gto.nyql.exceptions.NyException;

/**
 * @author iweerarathna
 */
public class NyScriptExecutionException extends NyException {

    public NyScriptExecutionException(String message) {
        super(message);
    }

    public NyScriptExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NyScriptExecutionException(String scriptId, String file, Throwable inner) {
        super(String.format(
                "Query script cannot be parsed due to syntax errors! ['%s', %s]", scriptId, file),
            inner);
    }

}