package com.virtusa.gto.nyql.engine.transform

import com.virtusa.gto.nyql.model.units.AParam
import com.virtusa.gto.nyql.model.units.NamedParam
import groovy.transform.CompileStatic

@java.lang.SuppressWarnings('JdbcResultSetReference')
import java.sql.ResultSet
@java.lang.SuppressWarnings('JdbcStatementReference')
import java.sql.Statement

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class JdbcCallResultTransformer implements QResultTransformer<JdbcCallTransformInput, Map> {

    private final JdbcResultTransformer rsTransformer = new JdbcResultTransformer()

    @Override
    Map apply(JdbcCallTransformInput input) {
        Statement statement = input.statement
        boolean hasResults = statement.execute()
        List rsList = []

        while (hasResults) {
            ResultSet rs = statement.getResultSet()
            rsList.add(rsTransformer.apply(rs))

            hasResults = statement.getMoreResults()
        }

        // check for out parameters
        Map op = [:]
        if (input.script.proxy?.orderedParameters) {
            for (int i = 0; i < input.script.proxy.orderedParameters.size(); i++) {
                NamedParam param = input.script.proxy.orderedParameters[i] as NamedParam
                if (param.scope != null && (param.scope == AParam.ParamScope.INOUT || param.scope == AParam.ParamScope.OUT)) {
                    op.put(param.__mappingParamName, statement.getObject(param.__mappingParamName))
                }
            }
        }

        if (rsList.isEmpty()) {
            [outs: op, result: null]
        } else if (rsList.size() == 1) {
            [outs: op, result: rsList[0]]
        } else {
            [outs: op, result: rsList]
        }
    }

    @Override
    long convertUpdateResult(long val) {
        val
    }
}
