package com.virtusa.gto.nyql.engine.pool

import com.virtusa.gto.nyql.exceptions.NyException

@java.lang.SuppressWarnings('JdbcConnectionReference')
import java.sql.Connection

/**
 * @author IWEERARATHNA
 */
interface QJdbcPoolFetcher {

    Connection getConnection() throws NyException
}