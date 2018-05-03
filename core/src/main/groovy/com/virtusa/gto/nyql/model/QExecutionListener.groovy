package com.virtusa.gto.nyql.model

import groovy.transform.CompileStatic

import java.sql.Statement

/**
 * @author iweerarathna
 */
@CompileStatic
interface QExecutionListener {

    void onBeforeExecution(Statement jdbcStatement)

    void onBeforeClosing(Statement jdbcStatement)

}
