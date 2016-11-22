package com.virtusa.gto.nyql.configs

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
    static final JDBC_DATASOURCE_CLASS_KEY = 'jdbcDataSourceClass'

    static final QUERY_TIMESTAMP_FORMAT = 'inputTimestampFormat'
    static final QUERY_TIMESTAMP_LOCALE = 'inputTimestampLocale'

    static final List<String> DEFAULT_EXTENSIONS = Collections.unmodifiableList(['groovy', 'sgroovy'])

    static final String SYS_JDBC_URL = 'com.virtusa.gto.nyql.JDBC_URL'
    static final String SYS_JDBC_USERNAME = 'com.virtusa.gto.nyql.JDBC_USERNAME'
    static final String SYS_JDBC_PASSWORD = 'com.virtusa.gto.nyql.JDBC_PASSWORD'
    static final String SYS_JDBC_DRIVER = 'com.virtusa.gto.nyql.JDBC_DRIVER'
    static final String SYS_ACTIVE_DB = 'com.virtusa.gto.nyql.ACTIVE_DB'

}
