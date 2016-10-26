package com.virtusa.gto.nyql.utils

import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
enum QOperator {

    EQUAL('='),
    NOT_EQUAL('<>'),
    GREATER_THAN('>'),
    GREATER_THAN_EQUAL('>='),
    LESS_THAN('<'),
    LESS_THAN_EQUAL('<='),
    IN('IN'),
    NOT_IN('NOT IN'),
    IS('IS'),
    IS_NOT('IS NOT'),

    EXISTS('EXISTS'),
    NOT_EXISTS('NOT EXISTS'),

    UNKNOWN('');

    private final String op

    private QOperator(final String opStr) {
        op = opStr
    }

    String getOp() {
        return op
    }
}