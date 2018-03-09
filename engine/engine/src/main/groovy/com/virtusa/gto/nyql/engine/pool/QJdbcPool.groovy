package com.virtusa.gto.nyql.engine.pool

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.exceptions.NyException
/**
 * @author IWEERARATHNA
 */
interface QJdbcPool extends QJdbcPoolFetcher {

    String getName()

    void init(Map options, Configurations configurations) throws NyException

    void shutdown() throws NyException

}
