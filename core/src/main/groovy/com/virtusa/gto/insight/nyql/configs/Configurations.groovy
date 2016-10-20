package com.virtusa.gto.insight.nyql.configs

import com.virtusa.gto.insight.nyql.db.QDbFactory
import com.virtusa.gto.insight.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.*
import com.virtusa.gto.insight.nyql.model.impl.QNoProfiling
import com.virtusa.gto.insight.nyql.model.impl.QProfExecutorFactory
import com.virtusa.gto.insight.nyql.model.impl.QProfRepository
import com.virtusa.gto.insight.nyql.utils.Constants
import com.virtusa.gto.insight.nyql.utils.QUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Constructor

/**
 * @author IWEERARATHNA
 */
class Configurations {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configurations)

    private Map properties = [:]

    private String cacheVarName
    private final Object lock = new Object()
    private boolean configured = false
    private ClassLoader classLoader
    private QProfiling profiler

    private Configurations() {}

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
        boolean profileEnabled = loadProfiler()
        if (!profileEnabled) {
            LOGGER.warn('Query profiling has been disabled! You might not be able to figure out timing of executions.')
        } else {
            LOGGER.debug("Query profiling enabled with ${profiler.getClass().simpleName}!")
            profiler.start(properties.profiling?.options ?: [:])
        }

        def factoryClasses = getAvailableTranslators()
        if (QUtils.notNullNorEmpty(factoryClasses)) {
            factoryClasses.each { loadDBFactory it }
        }

        // mark active database
        String activeDb = getActivatedDb()
        if (activeDb != null) {
            LOGGER.debug("Activating DB: $activeDb")
            QDatabaseRegistry.instance.load(activeDb)
        } else {
            throw new NyException('No database has been activated!')
        }

        // load repositories
        loadRepos(profileEnabled)

        // load executors
        loadExecutors(activeDb, profileEnabled)
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
            QRepository qRepository = (QRepository) classLoader.loadClass(String.valueOf(r.repo)).newInstance([scriptMapper].toArray())

            if (profEnabled) {
                qRepository = new QProfRepository(qRepository)
            }
            QRepositoryRegistry.getInstance().register(String.valueOf(r.name), qRepository, thisDef)
            added++
        }

        if (properties[ConfigKeys.REPO_MAP]) {
            Map<String, QRepository> repositoryMap = (Map<String, QRepository>) properties[ConfigKeys.REPO_MAP]
            repositoryMap.each {
                QRepository qRepository = profEnabled ? new QProfRepository(it.value) : it.value
                QRepositoryRegistry.instance.register(it.key, qRepository, it.key == defRepo)
                added++
            }
        }
    }

    private void loadExecutors(String activeDb, boolean profEnabled=false) {
        QDbFactory activeFactory = QDatabaseRegistry.instance.getDbFactory(activeDb)
        boolean loadDefOnly = properties.loadDefaultExecutorOnly ?: false
        String defExec = properties.defaultExecutor ?: Constants.DEFAULT_EXECUTOR_NAME
        List execs = properties.executors ?: []
        for (Map r : execs) {
            boolean thisDef = r.name == defExec
            if (loadDefOnly && !thisDef) {
                LOGGER.warn("Executor '{}' will not load since it is not the default executor!", r.name)
                continue
            }

            r.put('jdbcDriverClass', activeFactory.driverClassName())
            Class<?> clazz = classLoader.loadClass(String.valueOf(r.factory))
            QExecutorFactory executorFactory = createExecFactoryInstance(clazz, r)

            if (profEnabled) {
                executorFactory = new QProfExecutorFactory(executorFactory)
            }
            executorFactory.init(r)
            QExecutorRegistry.getInstance().register(String.valueOf(r.name), executorFactory, thisDef)
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

    private static void loadDBFactory(Class factoryClz) throws NyConfigurationException {
        try {
            def factory = factoryClz.newInstance() as QDbFactory
            loadDBFactory(factory)
        } catch (ReflectiveOperationException ex) {
            throw new NyConfigurationException("Failed to initialize database factory class by name '${factoryClz.name}'!", ex)
        }
    }

    private static void loadDBFactory(QDbFactory qDbFactory) {
        QDatabaseRegistry.instance.register(qDbFactory)
    }

    boolean addShutdownHook() {
        return properties.addAutoShutdownHook ?: false
    }

    void shutdown() {
        LOGGER.debug('Shutting down nyql...')
        safeClose('Executors') { QExecutorRegistry.instance.shutdown() }
        safeClose('Repositories') { QRepositoryRegistry.instance.shutdown() }
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
        return cacheVarName
    }

    String[] defaultImports() {
        return properties.defaultImports
    }

    String getActivatedDb() {
        return properties.activate
    }

    boolean cacheRawScripts() {
        return (boolean) properties.caching.compiledScripts
    }

    boolean cacheGeneratedQueries() {
        return (boolean) properties.caching.generatedQueries
    }

    List getAvailableTranslators() {
        return properties.translators
    }

    QProfiling getProfiler() {
        profiler
    }

    static Configurations instance() {
        Holder.INSTANCE
    }

    private static class Holder {
        private static final Configurations INSTANCE = new Configurations()
    }

}
