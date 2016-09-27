package com.virtusa.gto.insight.nyql.engine.impl.pool

import com.virtusa.gto.insight.nyql.exceptions.NyException

import java.sql.Connection

/**
 * @author IWEERARATHNA
 */
interface QJdbcPool extends QJdbcPoolFetcher {

    void init(Map options) throws NyException

    void shutdown() throws NyException

}
