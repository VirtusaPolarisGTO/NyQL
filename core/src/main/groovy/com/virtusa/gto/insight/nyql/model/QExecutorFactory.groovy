package com.virtusa.gto.insight.nyql.model

import com.virtusa.gto.insight.nyql.exceptions.NyConfigurationException

/**
 * @author IWEERARATHNA
 */
interface QExecutorFactory {

    void init(Map options) throws NyConfigurationException

    QExecutor create()

    QExecutor createReusable()

    void shutdown()

}
