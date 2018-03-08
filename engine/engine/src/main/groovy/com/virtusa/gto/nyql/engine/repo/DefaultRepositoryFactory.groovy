package com.virtusa.gto.nyql.engine.repo

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.model.QRepository
import com.virtusa.gto.nyql.model.QRepositoryFactory
import com.virtusa.gto.nyql.model.QScriptMapper
import com.virtusa.gto.nyql.utils.Constants
import groovy.transform.CompileStatic

/**
 * @author iweerarathna
 */
@CompileStatic
class DefaultRepositoryFactory implements QRepositoryFactory {
    @Override
    String getName() {
        Constants.DEFAULT_REPOSITORY_IMPL
    }

    @Override
    QRepository create(Configurations configurations, QScriptMapper scriptMapper) {
        return new QRepositoryImpl(configurations, scriptMapper)
    }
}
