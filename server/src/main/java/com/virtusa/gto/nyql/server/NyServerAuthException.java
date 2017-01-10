package com.virtusa.gto.nyql.server;

/**
 * @author iweerarathna
 */
public class NyServerAuthException extends Exception {

    public NyServerAuthException() {
        this("You do not have enough permission to access this NyQL server!");
    }

    private NyServerAuthException(String message) {
        super(message);
    }
}
