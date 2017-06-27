package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.exceptions.NyConfigurationException

/**
 * @author IWEERARATHNA
 */
interface QExecutorFactory {

    DbInfo init(Map options, Configurations configurations) throws NyConfigurationException

    QExecutor create()

    QExecutor createReusable()

    void shutdown()

}
