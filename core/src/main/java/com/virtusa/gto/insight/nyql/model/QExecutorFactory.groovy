package com.virtusa.gto.insight.nyql.model

import com.virtusa.gto.insight.nyql.QExecutor

/**
 * @author IWEERARATHNA
 */
interface QExecutorFactory {

    void init(Map options)

    QExecutor create();

    void shutdown()

}
