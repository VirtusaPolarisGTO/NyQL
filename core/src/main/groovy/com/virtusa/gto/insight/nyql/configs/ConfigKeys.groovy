package com.virtusa.gto.insight.nyql.configs

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * @author IWEERARATHNA
 */
@CompileStatic
@Immutable
final class ConfigKeys {

    static final String REPO_MAP = '__repoMap'
    static final String SCRIPT_MAP = '__scriptMapper'

    static final String PROFILING = 'profiling'
    static final String DEFAULT_REPO = 'defaultRepository'
    static final String DEFAULT_EXECUTOR = 'defaultExecutor'
    static final String REPOSITORIES = 'repositories'
    static final String EXECUTORS = 'executors'
    static final String CACHING = 'caching'
    static final String DEFAULT_IMPORTS = 'defaultImports'
    static final String TRANSLATORS = 'translators'
    static final String ACTIVATE_DB = 'activate'

    static final String LOCATION_KEY = '_location'

    static final JDBC_DRIVER_CLASS_KEY = 'jdbcDriverClass'
}
