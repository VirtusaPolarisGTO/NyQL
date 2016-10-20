package com.virtusa.gto.insight.nyql.engine.pool

import com.virtusa.gto.insight.nyql.exceptions.NyException

import java.sql.Connection

/**
 * @author IWEERARATHNA
 */
interface QJdbcPoolFetcher {

    Connection getConnection() throws NyException
}