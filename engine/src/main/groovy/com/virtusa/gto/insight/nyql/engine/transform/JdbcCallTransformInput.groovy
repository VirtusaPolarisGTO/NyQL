package com.virtusa.gto.insight.nyql.engine.transform

import com.virtusa.gto.insight.nyql.model.QScript

import java.sql.CallableStatement

/**
 * @author IWEERARATHNA
 */
class JdbcCallTransformInput {

    CallableStatement statement
    QScript script

}
