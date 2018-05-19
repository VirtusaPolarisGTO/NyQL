package com.virtusa.gto.nyql.engine.exceptions;

import com.virtusa.gto.nyql.exceptions.NyException;

/**
 * @author iweerarathna
 */
public class NyScriptParseException extends NyException {

    public NyScriptParseException(String scriptId, String file, Throwable inner) {
        super(String.format("Query script cannot be parsed due to syntax errors! ['%s', %s]", scriptId, file), inner);
    }

}
