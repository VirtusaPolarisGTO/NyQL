package com.virtusa.gto.nyql.engine.exceptions;

/**
 * @author iweerarathna
 */
public class NyParamNotFoundException extends NyScriptExecutionException {
    public NyParamNotFoundException(String paramName) {
        super(String.format("Parameter '%s' not found in the script session!", paramName));
    }
}

