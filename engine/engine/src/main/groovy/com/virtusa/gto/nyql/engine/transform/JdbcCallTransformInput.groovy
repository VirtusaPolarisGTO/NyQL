package com.virtusa.gto.nyql.engine.transform

import com.virtusa.gto.nyql.model.QScript

import java.sql.CallableStatement

/**
 * @author IWEERARATHNA
 */
class JdbcCallTransformInput {

    CallableStatement statement
    QScript script

    void clear() {
        statement = null
        script = null
    }
}
