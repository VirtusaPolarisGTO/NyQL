package com.virtusa.gto.nyql.configs

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * @author IWEERARATHNA
 */
@CompileStatic
@Immutable
final class ConfigKeys {

    static final String GROOVY_EXT = '.groovy'

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

    static final String QUERIES_ROOT = 'queries'
    static final String QUERIES_KEYWORDS = 'keywordsPath'
    static final String QUERIES_MAPPINGS = 'nameMappingPath'

    static final String LOCATION_KEY = '_location'

    static final JDBC_DRIVER_CLASS_KEY = 'jdbcDriverClass'
    static final JDBC_DATASOURCE_CLASS_KEY = 'jdbcDataSourceClass'

    static final QUERY_TIMESTAMP_FORMAT = 'inputTimestampFormat'
    static final QUERY_TIMESTAMP_LOCALE = 'inputTimestampLocale'

    static final List<String> DEFAULT_EXTENSIONS = Collections.unmodifiableList(['groovy', 'sgroovy'])

    static final String SYS_JDBC_URL = 'NYQL_JDBC_URL'
    static final String SYS_JDBC_USERNAME = 'NYQL_JDBC_USERNAME'
    static final String SYS_JDBC_PASSWORD = 'NYQL_JDBC_PASSWORD'
    static final String SYS_JDBC_PASSWORD_ENC = 'NYQL_JDBC_PASSWORD_ENC'
    static final String SYS_JDBC_DRIVER = 'NYQL_JDBC_DRIVER'
    static final String SYS_ACTIVE_DB = 'NYQL_ACTIVE_DB'
    static final String SYS_SCRIPT_DIR = 'NYQL_SCRIPT_DIR'
    static final String SYS_CONNECT_RETRY_COUNT = 'NYQL_CONNECT_RETRY_COUNT'
    static final String SYS_CONNECT_RETRY_INTERVAL = 'NYQL_CONNECT_RETRY_INTERVAL'
    static final String SYS_CACHE_RAW_ENABLED = 'NYQL_CACHE_RAW_ENABLED'
    static final String SYS_CACHE_QUERY_ENABLED = 'NYQL_CACHE_QUERY_ENABLED'
}
