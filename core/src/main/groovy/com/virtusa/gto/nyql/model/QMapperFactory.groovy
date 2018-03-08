package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.configs.Configurations
import groovy.transform.CompileStatic

/**
 * @author iweerarathna
 */
@CompileStatic
interface QMapperFactory {

    QScriptMapper create(Map args, Configurations configurations)

}
