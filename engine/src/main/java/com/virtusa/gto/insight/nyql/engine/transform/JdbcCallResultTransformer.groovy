package com.virtusa.gto.insight.nyql.engine.transform

import com.virtusa.gto.insight.nyql.model.params.AParam

import java.sql.ResultSet

/**
 * @author IWEERARATHNA
 */
class JdbcCallResultTransformer implements QResultTransformer<JdbcCallTransformInput, Map> {

    private final JdbcResultTransformer rsTransformer = new JdbcResultTransformer()

    @Override
    Map apply(JdbcCallTransformInput input) {
        boolean hasResults = input.statement.execute()
        List rsList = []

        while (hasResults) {
            ResultSet rs = input.statement.getResultSet()
            rsList.add(rsTransformer.apply(rs))

            hasResults = input.statement.getMoreResults()
        }

        // check for out parameters
        Map op = [:]
        for (int i = 0; i < input.script.proxy.orderedParameters.size(); i++) {
            AParam param = input.script.proxy.orderedParameters[i]
            if (param.scope != null && (param.scope == AParam.ParamScope.INOUT || param.scope == AParam.ParamScope.OUT)) {
                op.put(param.__mappingParamName, input.statement.getObject(param.__mappingParamName))
            }
        }

        if (rsList.isEmpty()) {
            return [outs: op, r: null]
        } else if (rsList.size() == 1) {
            return [outs: op, r: rsList[0]]
        } else {
            return [outs: op, r: rsList]
        }
    }

    @Override
    long convertUpdateResult(long val) {
        return val
    }
}
