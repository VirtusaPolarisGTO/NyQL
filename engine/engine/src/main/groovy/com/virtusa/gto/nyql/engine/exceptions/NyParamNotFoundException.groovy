package com.virtusa.gto.nyql.engine.exceptions

/**
 * An exception thrown when required parameter is not found within execution context.
 *
 * @author iweerarathna
*/
class NyParamNotFoundException extends NyScriptExecutionException {
    NyParamNotFoundException(String paramName) {
        super("Parameter $paramName not found in the script session!")
    }
}
