package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.configs.Configurations

/**
 * @author iweerarathna
 */
interface QRepositoryFactory {

    String getName()

    QRepository create(Configurations configurations, QScriptMapper scriptMapper)

}
