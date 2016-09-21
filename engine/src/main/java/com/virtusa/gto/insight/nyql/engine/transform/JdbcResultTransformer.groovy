package com.virtusa.gto.insight.nyql.engine.transform

import java.sql.ResultSet
import java.sql.ResultSetMetaData

/**
 * @author IWEERARATHNA
 */
class JdbcResultTransformer implements QJdbcResultTransformer<List<Map<String, Object>>> {

    private boolean doAutoClose

    JdbcResultTransformer(boolean autoClose=true) {
        doAutoClose = autoClose
    }

    @Override
    List<Map<String, Object>> apply(ResultSet resultSet) {
        try {
            ResultSetMetaData metaData = resultSet.getMetaData()
            int cc = metaData.columnCount
            Map<Integer, String> cols = new HashMap<>()
            for (int i = 1; i <= cc; i++) {
                cols.put(i, metaData.getColumnLabel(i))
            }

            List<Map<String, Object>> result = new LinkedList<>()
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>()
                for (int i = 1; i <= cc; i++) {
                    row.put(cols[i], resultSet.getObject(i))
                }
                result.add(row)
            }
            return result

        } finally {
            if (doAutoClose) {
                resultSet.close()
            }
        }
    }

}
