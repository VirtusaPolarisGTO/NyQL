package com.virtusa.gto.nyql.engine.transform

import com.virtusa.gto.nyql.model.QScript
import groovy.transform.CompileStatic

@java.lang.SuppressWarnings('JdbcStatementReference')
import java.sql.CallableStatement

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class JdbcCallTransformInput {

    CallableStatement statement
    QScript script

    void clear() {
        statement = null
        script = null
    }
}
