package com.virtusa.gto.nyql.engine.transform

import groovy.transform.CompileStatic

@java.lang.SuppressWarnings('JdbcResultSetReference')
import java.sql.ResultSet
import java.sql.ResultSetMetaData
/**
 * @author IWEERARATHNA
 */
@CompileStatic
class JdbcResultTransformer implements QJdbcResultTransformer<List<Map<String, Object>>> {

    List<Map<String, Object>> apply(ResultSet resultSet) {
        try {
            ResultSetMetaData metaData = resultSet.getMetaData()
            int cc = metaData.columnCount
            Map<Integer, String> cols = [:]
            for (int i = 1; i <= cc; i++) {
                cols.put(i, metaData.getColumnLabel(i))
            }

            List<Map<String, Object>> result = [] as LinkedList
            while (resultSet.next()) {
                Map<String, Object> row = [:]
                for (int i = 1; i <= cc; i++) {
                    row.put(cols[i], resultSet.getObject(i))
                }
                result.add(row)
            }
            return result

        } finally {
            resultSet.close()
        }
    }

    @Override
    long convertUpdateResult(long val) {
        val
    }
}
