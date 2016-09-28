package com.virtusa.gto.insight.nyql.model
/**
 * @author IWEERARATHNA
 */
interface QExecutorFactory {

    void init(Map options)

    QExecutor create();

    QExecutor createReusable();

    void shutdown()

}
