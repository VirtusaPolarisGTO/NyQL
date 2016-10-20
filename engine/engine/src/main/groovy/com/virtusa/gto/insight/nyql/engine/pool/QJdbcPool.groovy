package com.virtusa.gto.insight.nyql.engine.pool

import com.virtusa.gto.insight.nyql.exceptions.NyException

/**
 * @author IWEERARATHNA
 */
interface QJdbcPool extends QJdbcPoolFetcher {

    void init(Map options) throws NyException

    void shutdown() throws NyException

}
