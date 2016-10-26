package com.virtusa.gto.nyql.engine.transform

import java.sql.ResultSet

/**
 * @author IWEERARATHNA
 */
interface QJdbcResultTransformer<R> extends QResultTransformer<ResultSet, R> {

}
