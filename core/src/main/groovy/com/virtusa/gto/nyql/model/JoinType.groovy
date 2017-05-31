package com.virtusa.gto.nyql.model

import groovy.transform.CompileStatic

/**
 * @author iweerarathna
 */
@CompileStatic
enum JoinType {

    CROSS_JOIN('CROSS JOIN'),
    LEFT_JOIN('LEFT JOIN'),
    RIGHT_JOIN('RIGHT JOIN'),
    FULL_JOIN('FULL JOIN'),
    INNER_JOIN('INNER JOIN')

    private final String joinName

    JoinType(String joinName) {
        this.joinName = joinName
    }

    String getJoinName() {
        return joinName
    }
}