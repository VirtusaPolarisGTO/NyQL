package com.virtusa.gto.nyql.configs

import com.virtusa.gto.nyql.db.QDbFactory
import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.QDatabaseRegistry
import com.virtusa.gto.nyql.model.QExecutorFactory
import com.virtusa.gto.nyql.model.QExecutorRegistry
import com.virtusa.gto.nyql.model.QProfiling
import com.virtusa.gto.nyql.model.QRepository
import com.virtusa.gto.nyql.model.QRepositoryRegistry
import com.virtusa.gto.nyql.model.QScriptMapper
import com.virtusa.gto.nyql.model.impl.QNoProfiling
import com.virtusa.gto.nyql.model.impl.QProfExecutorFactory
import com.virtusa.gto.nyql.model.impl.QProfRepository
import com.virtusa.gto.nyql.utils.Constants
import com.virtusa.gto.nyql.utils.QUtils
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Constructor
import java.time.format.DateTimeFormatter

/**
 * @author IWEERARATHNA
 */
class Configurations {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configurations)

    private DateTimeFormatter timestampFormatter = DateTimeFormatter.ISO_INSTANT

    private Map properties = [:]

    private String cacheVarName
    private final Object lock = new Object()
    private boolean configured = false
    private ClassLoader classLoader
    private QProfiling profiler

    private QDatabaseRegistry databaseRegistry
    private QExecutorRegistry executorRegistry
    private QRepositoryRegistry repositoryRegistry

    @PackageScope Configurations() {}

    Configurations configure(Map configProps) throws NyException {
        properties = configProps
        classLoader = Thread.currentThread().contextClassLoader

        synchronized (lock) {
            doConfig()
            configured = true
        }
        return this
    }

    boolean isConfigured() {
        synchronized (lock) {
            return configured
        }
    }

    private void doConfig() throws NyException {
        databaseRegistry = QDatabaseRegistry.newInstance()
        executorRegistry = QExecutorRegistry.newInstance()
        repositoryRegistry = QRepositoryRegistry.newInstance()

        // load query related configurations
        loadQueryInfo(properties.queries as Map)

        boolean profileEnabled = loadProfiler()
        if (!profileEnabled) {
            LOGGER.warn('Query profiling has been disabled! You might not be able to figure out timing of executions.')
        } else {
            LOGGER.debug("Query profiling enabled with ${profiler.getClass().simpleName}!")
            Map profOptions = properties.profiling?.options ?: [:]
            profOptions['isCached'] = properties.caching.compiledScripts
            profiler.start(profOptions)
        }

        // mark active database
        String activeDb = getActivatedDb()
        LOGGER.info("Activated: " + activeDb)
        loadActivatedTranslator(activeDb)

        // load repositories
        loadRepos(profileEnabled)

        // load executors
        loadExecutors(activeDb, profileEnabled)

        // finally, initialize factory
        databaseRegistry.getDbFactory(activeDb).init(this)
    }

    private void loadActivatedTranslator(String activeDb) {
        if (activeDb == null) {
            throw new NyConfigurationException('No database has been specified to be activated!')
        }

        def factoryClasses = getAvailableTranslators()
        if (QUtils.notNullNorEmpty(factoryClasses)) {
            Exception firstEx = null
            for (def tr : factoryClasses) {
                try {
                    loadDBFactory(tr)
                } catch (ReflectiveOperationException | NyConfigurationException ex) {
                    LOGGER.warn(ex.getMessage())
                    if (firstEx == null) {
                        firstEx = ex
                    }
                }
            }
            databaseRegistry.load(activeDb)

        } else {
            throw new NyConfigurationException('No NyQL translators have been specified in the configuration file!')
        }
    }

    @CompileStatic
    private void loadQueryInfo(Map options) {
        if (options != null && options.parameters) {
            Map paramConfig = options.parameters as Map
            String tsFormat = paramConfig[ConfigKeys.QUERY_TIMESTAMP_FORMAT]
            if (tsFormat != null && !tsFormat.isEmpty()) {
                String tsLocale = paramConfig[ConfigKeys.QUERY_TIMESTAMP_LOCALE]
                timestampFormatter = DateTimeFormatter.ofPattern(tsFormat,
                        tsLocale == null ? Locale.default : Locale.forLanguageTag(tsLocale))

                LOGGER.debug('JDBC executor uses time-format ' + tsFormat + ' in ' + (tsLocale ?: 'system default') + ' locale.')
            }
        } else {
            LOGGER.info('JDBC executor uses Java ISO Instant time-format in system default locale.')
        }
    }

    private boolean loadProfiler() throws NyConfigurationException {
        def profiling = properties[ConfigKeys.PROFILING]
        if (profiling?.enabled) {
            def prof = profiling.profiler
            if (prof instanceof QProfiling) {
                profiler = prof
            } else {
                try {
                    profiler = classLoader.loadClass(String.valueOf(prof)).newInstance() as QProfiling
                } catch (ReflectiveOperationException ex) {
                    throw new NyConfigurationException("Error occurred while loading profiler! $prof", ex)
                }
            }
            return true

        } else {
            profiler = QNoProfiling.INSTANCE
            return false
        }
    }

    private void loadRepos(boolean profEnabled=false) {
        int added = 0
        String defRepo = properties.defaultRepository ?: Constants.DEFAULT_REPOSITORY_NAME
        List repos = properties.repositories ?: []
        for (Map r : repos) {
            Map args = r.mapperArgs ?: [:]
            args.put('_location', properties._location)

            boolean thisDef = r.name == defRepo
            QScriptMapper scriptMapper = classLoader.loadClass(String.valueOf(r.mapper)).createNew(args)
            QRepository qRepository = (QRepository) classLoader.loadClass(String.valueOf(r.repo))
                    .newInstance([this, scriptMapper].toArray())

            if (profEnabled) {
                qRepository = new QProfRepository(this, qRepository)
            }
            repositoryRegistry.register(String.valueOf(r.name), qRepository, thisDef)
            added++
        }

        if (properties[ConfigKeys.REPO_MAP]) {
            Map<String, QRepository> repositoryMap = (Map<String, QRepository>) properties[ConfigKeys.REPO_MAP]
            repositoryMap.each {
                QRepository qRepository = profEnabled ? new QProfRepository(this, it.value) : it.value
                repositoryRegistry.register(it.key, qRepository, it.key == defRepo)
                added++
            }
        }
    }

    private void loadExecutors(String activeDb, boolean profEnabled=false) {
        QDbFactory activeFactory = databaseRegistry.getDbFactory(activeDb)
        boolean loadDefOnly = properties.loadDefaultExecutorOnly ?: false
        String defExec = properties.defaultExecutor ?: Constants.DEFAULT_EXECUTOR_NAME
        List execs = properties.executors ?: []
        for (Map r : execs) {
            boolean thisDef = r.name == defExec
            if (loadDefOnly && !thisDef) {
                LOGGER.warn("Executor '{}' will not load since it is not the default executor!", r.name)
                continue
            }

            r.put(ConfigKeys.JDBC_DRIVER_CLASS_KEY, r.jdbcDriverClass ?: activeFactory.driverClassName())
            r.put(ConfigKeys.JDBC_DATASOURCE_CLASS_KEY, r.jdbcDataSourceClass ?: activeFactory.dataSourceClassName())
            Class<?> clazz = classLoader.loadClass(String.valueOf(r.factory))
            QExecutorFactory executorFactory = createExecFactoryInstance(clazz, r)

            if (profEnabled) {
                executorFactory = new QProfExecutorFactory(this, executorFactory)
            }
            executorFactory.init(r)
            executorRegistry.register(String.valueOf(r.name), executorFactory, thisDef)
        }
    }

    private static QExecutorFactory createExecFactoryInstance(Class<?> clz, Map options) {
        try {
            Constructor<?> constructor = clz.getDeclaredConstructor(Map)
            (QExecutorFactory) constructor.newInstance(options)
        } catch (NoSuchMethodException ignored) {
            (QExecutorFactory) clz.newInstance()
        }
    }

    private void loadDBFactory(String clzName) throws NyConfigurationException {
        try {
            loadDBFactory(classLoader.loadClass(clzName))
        } catch (ReflectiveOperationException ex) {
            throw new NyConfigurationException("No database factory implementation class found by name '$clzName'!", ex)
        }
    }

    private void loadDBFactory(Class factoryClz) throws NyConfigurationException {
        try {
            QDbFactory factory = factoryClz.newInstance() as QDbFactory
            loadDBFactory(factory)
        } catch (ReflectiveOperationException ex) {
            throw new NyConfigurationException("Failed to initialize database factory class by name '${factoryClz.name}'!", ex)
        }
    }

    private void loadDBFactory(QDbFactory qDbFactory) {
        databaseRegistry.register(qDbFactory)
    }

    void shutdown() {
        LOGGER.debug('Shutting down nyql...')
        safeClose('Executors') { executorRegistry.shutdown() }
        safeClose('Repositories') { repositoryRegistry.shutdown() }
        safeClose('Profiler') { profiler.close() }
        synchronized (lock) {
            configured = false
        }
        ConfigBuilder.instance().reset()
        LOGGER.debug('Shutdown completed.')
    }

    @SuppressWarnings('CatchThrowable')
    private static void safeClose(String workerName, Runnable runnable) {
        try {
            runnable.run()
        } catch (Throwable ignored) {
            LOGGER.error('Failed to close ' + workerName + '!', ignored)
        }
    }

    String cachingIndicatorVarName() {
        if (cacheVarName != null) {
            return cacheVarName
        }
        cacheVarName = properties.caching.indicatorVariableName ?: Constants.DSL_CACHE_VARIABLE_NAME
        cacheVarName
    }

    String[] defaultImports() {
        properties.defaultImports
    }

    String getActivatedDb() {
        (String) System.getProperty(ConfigKeys.SYS_ACTIVE_DB, properties.activate)
    }

    boolean cacheRawScripts() {
        (boolean) properties.caching.compiledScripts
    }

    boolean cacheGeneratedQueries() {
        (boolean) properties.caching.generatedQueries
    }

    List getAvailableTranslators() {
        properties.translators
    }

    List getSupportedScriptExtensions() {
        properties.supportedExtensions ?: ConfigKeys.DEFAULT_EXTENSIONS
    }

    Map getAllProperties() {
        properties
    }

    QProfiling getProfiler() {
        profiler
    }

    QDatabaseRegistry getDatabaseRegistry() {
        databaseRegistry
    }

    QExecutorRegistry getExecutorRegistry() {
        executorRegistry
    }

    QRepositoryRegistry getRepositoryRegistry() {
        repositoryRegistry
    }

    DateTimeFormatter getTimestampFormatter() {
        timestampFormatter
    }

    static Configurations instance() {
        Holder.INSTANCE
    }

    private static class Holder {
        private static final Configurations INSTANCE = new Configurations()
    }

}
