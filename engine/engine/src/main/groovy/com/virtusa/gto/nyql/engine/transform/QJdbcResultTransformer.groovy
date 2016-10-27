package com.virtusa.gto.nyql.engine.transform

@java.lang.SuppressWarnings('JdbcResultSetReference')
import java.sql.ResultSet

/**
 * @author IWEERARATHNA
 */
interface QJdbcResultTransformer<R> extends QResultTransformer<ResultSet, R> {

}
