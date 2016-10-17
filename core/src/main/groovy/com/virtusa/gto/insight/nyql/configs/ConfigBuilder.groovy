package com.virtusa.gto.insight.nyql.configs

import com.virtusa.gto.insight.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.insight.nyql.model.QProfiling
import com.virtusa.gto.insight.nyql.model.QRepository
import com.virtusa.gto.insight.nyql.model.QScriptMapper

/**
 * @author IWEERARATHNA
 */
class ConfigBuilder {

    private boolean hasInitialized = false
    private final Object lock = new Object()
    private Map props = [:]
    private Map<String, QRepository> repositoryMap = [:]
    private Map<String, QScriptMapper> scriptMapper = [:]

    private ConfigBuilder() {}

    /**
     * Enable profiler in nyql. Profiler will passively run and notifies
     * about parsing time or running time of each query.
     *
     * @param profilingImpl the profiler instance to be used when nyql running.
     * @return this config builder instance.
     */
    ConfigBuilder enableProfiler(QProfiling profilingImpl) {
        if (!props[ConfigKeys.PROFILING]) {
            props[ConfigKeys.PROFILING] = [:]
        }
        props[ConfigKeys.PROFILING].enabled = true
        props[ConfigKeys.PROFILING].profiler = profilingImpl
        this
    }

    /**
     * Set the default repository by name.
     *
     * @param repoName repository name to make default.
     * @return this config builder instance.
     */
    ConfigBuilder havingDefaultRepository(String repoName) {
        assertNotInitialized()
        props[ConfigKeys.DEFAULT_REPO] = repoName
        this
    }

    /**
     * Adds a script mapper class which responsible of loading/providing relevant
     * scripts to a repository.
     *
     * @param mapperName unique name of the mapper.
     * @param qScriptMapper script mapper instance.
     * @return this config builder instance.
     */
    ConfigBuilder addScriptLoader(String mapperName, QScriptMapper qScriptMapper) {
        scriptMapper.put(mapperName, qScriptMapper)
        this
    }

    /**
     * Returns the script mapper instance represent by given name.
     *
     * @param mapperName name of the mapper.
     * @return mapper instance or null if no such mapper exist.
     */
    QScriptMapper getScriptLoader(String mapperName) {
        scriptMapper.get(mapperName)
    }

    /**
     * Returns the repository instance represent by this given name.
     *
     * @param name name of the repository.
     * @return repository instance or null if no such repository found.
     */
    QRepository getRepository(String name) {
        repositoryMap[name]
    }

    /**
     * Adds a new repository.
     *
     * @param name name of the repository.
     * @param repository repository instance.
     * @return this config builder instance.
     */
    ConfigBuilder addRepository(String name, QRepository repository) {
        repositoryMap.put(name, repository)
        this
    }

    /**
     * Add repository by using a map instance.
     * <p>
     * <b>WARN:</b>
     * It is strongly recommend to use other {@Link #addRepository} method.
     * </p>
     *
     * @param repositoryConfig repository options as map
     * @return this config builder instance.
     */
    ConfigBuilder addRepository(Map repositoryConfig) {
        assertNotInitialized()
        if (!props[ConfigKeys.REPOSITORIES]) {
            props[ConfigKeys.REPOSITORIES] = []
        }
        props[ConfigKeys.REPOSITORIES] << repositoryConfig
        this
    }

    /**
     * Sets the default executor instance for this nyql configuration.
     *
     * @param executorName executor name to make it as default.
     * @return this config builder instance.
     */
    ConfigBuilder havingDefaultExecutor(String executorName) {
        assertNotInitialized()
        props[ConfigKeys.DEFAULT_EXECUTOR] = executorName
        this
    }

    /**
     * Adds a new executor instance to the nyql.
     *
     * @param executorConfigs nyql executor as a map.
     * @return this config builder instance.
     */
    ConfigBuilder addExecutor(Map executorConfigs) {
        assertNotInitialized()
        if (!props[ConfigKeys.EXECUTORS]) {
            props[ConfigKeys.EXECUTORS] = []
        }
        props[ConfigKeys.EXECUTORS] << executorConfigs
        this
    }

    /**
     * Enables caching of generated queries.
     *
     * @return this config builder instance.
     */
    ConfigBuilder doCacheGeneratedQueries() {
        assertNotInitialized()
        if (!props[ConfigKeys.CACHING]) {
            props[ConfigKeys.CACHING] = [:]
        }
        props[ConfigKeys.CACHING].generatedQueries = true
        this
    }

    /**
     * Enables caching of compiled scripts.
     *
     * @return this config builder instance.
     */
    ConfigBuilder doCacheCompiledScripts() {
        assertNotInitialized()
        if (!props[ConfigKeys.CACHING]) {
            props[ConfigKeys.CACHING] = [:]
        }
        props[ConfigKeys.CACHING].compiledScripts = true
        this
    }

    /**
     * Adds a set of default imports to be for the all scripts.
     *
     * @param clzFullName a set of full classnames to import for all scripts.
     * @return this config builder instance.
     */
    ConfigBuilder addDefaultImporters(String... clzFullName) {
        assertNotInitialized()
        clzFullName.each { addDefaultImporter(it) }
        this
    }

    /**
     * Adds a set of default imports to be for the all scripts.
     *
     * @param clzFullName a collection of full classnames to import for all scripts.
     * @return this config builder instance.
     */
    ConfigBuilder addDefaultImporters(Collection<String> clzFullName) {
        assertNotInitialized()
        clzFullName.each { addDefaultImporter(it) }
        this
    }

    /**
     * Adds a default imports to be for the all scripts.
     *
     * @param clzFullName a full classname to import for all scripts.
     * @return this config builder instance.
     */
    ConfigBuilder addDefaultImporter(String clzFullName) {
        assertNotInitialized()
        if (!props[ConfigKeys.DEFAULT_IMPORTS]) {
            props[ConfigKeys.DEFAULT_IMPORTS] = []
        }
        props[ConfigKeys.DEFAULT_IMPORTS] << clzFullName
        this
    }

    /**
     * Adds a set of query translators for parsing. All translators must implement
     * <code>QTranslator</code> interface.
     *
     * @param clzFullName a set of full classnames as translators.
     * @return this config builder instance.
     */
    ConfigBuilder addTranslators(Collection<String> clzNamesList) {
        clzNamesList.each { addTranslator(it) }
        this
    }

    /**
     * Adds a query translator for parsing. All translators must implement
     * <code>QTranslator</code> interface.
     *
     * @param clzFullName a full classnames as translator.
     * @return this config builder instance.
     */
    ConfigBuilder addTranslator(String fullClzName) {
        assertNotInitialized()
        props[ConfigKeys.TRANSLATORS] ? props[ConfigKeys.TRANSLATORS].add(fullClzName) : [fullClzName]
        this
    }

    /**
     * Set the activate database for nyql.
     *
     * @param dbImplName name of the db to make active.
     * @return this config builder instance.
     */
    ConfigBuilder activateDb(String dbImplName) {
        assertNotInitialized()
        props[ConfigKeys.ACTIVATE_DB] = dbImplName
        this
    }

    private void assertNotInitialized() {
        synchronized (lock) {
            if (hasInitialized) {
                throw new NyConfigurationException("NyQL already has configured! You can't change after initialization! Sorry.")
            }
        }
    }

    /**
     * Input map to initialize the configuration of nyql. This could be parsed
     * from a json file.
     *
     * @param map a map containing initial configuration set.
     * @return this config builder instance.
     */
    ConfigBuilder setupFrom(Map map) {
        props.putAll(map)
        this
    }

    /**
     * Build the current configurations for nyql.
     *
     * @return initialized configuration instance.
     */
    Configurations build() {
        props[ConfigKeys.REPO_MAP] = repositoryMap
        props[ConfigKeys.SCRIPT_MAP] = scriptMapper

        synchronized (lock) {
            hasInitialized = true
        }
        Configurations.instance().configure(props)
    }

    /**
     * Returns the unique instance of config builder.
     *
     * @return an instance of config builder.
     */
    static ConfigBuilder instance() {
        Holder.INSTANCE
    }

    private static class Holder {
        private static final ConfigBuilder INSTANCE = new ConfigBuilder()
    }

}
