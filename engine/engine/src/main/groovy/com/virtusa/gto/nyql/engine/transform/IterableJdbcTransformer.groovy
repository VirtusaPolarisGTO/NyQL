package com.virtusa.gto.nyql.engine.transform

import com.virtusa.gto.nyql.engine.impl.NyQLResult
import com.virtusa.gto.nyql.engine.impl.QJdbcExecutor
import com.virtusa.gto.nyql.model.QPagedScript
import groovy.transform.CompileStatic

import java.sql.ResultSet
/**
 * @author iweerarathna
 */
@CompileStatic
class IterableJdbcTransformer implements QJdbcResultTransformer<Iterable<NyQLResult>> {

    private QJdbcExecutor jdbcExecutor
    private QPagedScript pagedScript

    IterableJdbcTransformer(QJdbcExecutor executor, QPagedScript script) {
        jdbcExecutor = executor
        pagedScript = script
    }

    @Override
    long convertUpdateResult(long val) {
        return val
    }

    @Override
    Iterable<NyQLResult> apply(ResultSet resultSet) {
        return new NyJdbcIterable(resultSet, jdbcExecutor, pagedScript)
    }
}
