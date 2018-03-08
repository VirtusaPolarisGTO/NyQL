package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.configs.Configurations
import groovy.transform.CompileStatic

/**
 * @author iweerarathna
 */
@CompileStatic
interface QMapperFactory {

    String[] supportedMappers()

    QScriptMapper create(String implName, Map args, Configurations configurations)

}
