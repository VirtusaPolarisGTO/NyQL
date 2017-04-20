package com.virtusa.gto.nyql.engine.exceptions

import com.virtusa.gto.nyql.exceptions.NyException
import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class NyScriptExecutionException extends NyException {

    NyScriptExecutionException(String message) {
        super(message)
    }

    NyScriptExecutionException(String message, Throwable cause) {
        super(message, cause)
    }

    NyScriptExecutionException(String scriptId, String file, Throwable inner) {
        super("Query script cannot be parsed due to syntax errors! ['$scriptId', ${file}]", inner)
    }

}
