package com.virtusa.gto.insight.nyql.configs

import com.virtusa.gto.insight.nyql.db.QDbFactory
import com.virtusa.gto.insight.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.*
import com.virtusa.gto.insight.nyql.utils.Constants
import com.virtusa.gto.insight.nyql.utils.QUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author IWEERARATHNA
 */
class Configurations {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configurations)

    private Map properties = [:]

    private String cacheVarName
    private final Object lock = new Object();
    private boolean configured = false
    private ClassLoader classLoader

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
            return configured;
        }
    }

    private void doConfig() throws NyException {
        def factoryClasses = getAvailableTranslators()
        if (QUtils.notNullNorEmpty(factoryClasses)) {
            factoryClasses.each { loadDBFactory it }
        }

        // mark active database
        String activeDb = getActivatedDb()
        if (activeDb != null) {
            LOGGER.debug("Activating DB: {}", activeDb)
            QDatabaseRegistry.instance.load(activeDb)
        } else {
            throw new NyException("No database has been activated!")
        }

        // load repositories
        loadRepos()

        // load executors
        loadExecutors(activeDb)
    }

    private void loadRepos() {
        int added = 0
        String defRepo = properties.defaultRepository ?: Constants.DEFAULT_REPOSITORY_NAME
        List repos = properties.repositories ?: []
        for (Map r : repos) {
            Map args = r.mapperArgs ?: [:]
            args.put("_location", properties._location)

            boolean thisDef = r.name == defRepo
            QScriptMapper scriptMapper = classLoader.loadClass(String.valueOf(r.mapper)).createNew(args)
            QRepository qRepository = (QRepository) classLoader.loadClass(String.valueOf(r.repo)).newInstance([scriptMapper].toArray())

            QRepositoryRegistry.getInstance().register(String.valueOf(r.name), qRepository, thisDef)
            added++
        }

        if (properties["__repoMap"]) {
            Map<String, QRepository> repositoryMap = (Map<String, QRepository>) properties["__repoMap"]
            repositoryMap.each { QRepositoryRegistry.instance.register(it.key, it.value, it.key == defRepo); added++ }
        }
    }

    private void loadExecutors(String activeDb) {
        QDbFactory activeFactory = QDatabaseRegistry.instance.getDbFactory(activeDb);
        boolean loadDefOnly = properties.loadDefaultExecutorOnly ?: false
        String defExec = properties.defaultExecutor ?: Constants.DEFAULT_EXECUTOR_NAME
        List execs = properties.executors ?: []
        for (Map r : execs) {
            boolean thisDef = r.name == defExec
            if (loadDefOnly && !thisDef) {
                LOGGER.warn("Executor '{}' will not load since it is not the default executor!", r.name)
                continue
            }

            QExecutorFactory executorFactory = (QExecutorFactory) classLoader.loadClass(String.valueOf(r.factory)).newInstance()
            r.put("jdbcDriverClass", activeFactory.driverClassName())
            executorFactory.init(r)

            QExecutorRegistry.getInstance().register(String.valueOf(r.name), executorFactory, thisDef)
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
        QExecutorRegistry.instance.shutdown()
        QRepositoryRegistry.instance.shutdown()
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

    boolean compileAtStartup() {
        return (boolean) (properties.caching.compileAtStartUp ?: false)
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

    static Configurations instance() {
        return Holder.INSTANCE
    }

    private static class Holder {
        private static final Configurations INSTANCE = new Configurations()
    }

}
