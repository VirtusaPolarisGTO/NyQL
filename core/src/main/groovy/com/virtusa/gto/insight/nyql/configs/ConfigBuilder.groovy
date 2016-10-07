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

    ConfigBuilder enableProfiler(QProfiling profilingImpl) {
        if (!props[ConfigKeys.PROFILING]) {
            props[ConfigKeys.PROFILING] = [:]
        }
        props[ConfigKeys.PROFILING].enabled = true
        props[ConfigKeys.PROFILING].profiler = profilingImpl
        return this
    }

    ConfigBuilder havingDefaultRepository(String repoName) {
        assertNotInitialized()
        props[ConfigKeys.DEFAULT_REPO] = repoName
        return this
    }

    ConfigBuilder addScriptLoader(String mapperName, QScriptMapper qScriptMapper) {
        scriptMapper.put(mapperName, qScriptMapper)
        return this
    }

    QScriptMapper getScriptLoader(String mapperName) {
        return scriptMapper.get(mapperName)
    }

    QRepository getRepository(String name) {
        return repositoryMap[name]
    }

    ConfigBuilder addRepository(String name, QRepository repository) {
        repositoryMap.put(name, repository)
        return this
    }

    ConfigBuilder addRepository(Map repositoryConfig) {
        assertNotInitialized()
        if (!props[ConfigKeys.REPOSITORIES]) {
            props[ConfigKeys.REPOSITORIES] = []
        }
        props[ConfigKeys.REPOSITORIES] << repositoryConfig
        return this
    }

    ConfigBuilder havingDefaultExecutor(String executorName) {
        assertNotInitialized()
        props[ConfigKeys.DEFAULT_EXECUTOR] = executorName
        return this
    }

    ConfigBuilder addExecutor(Map executorConfigs) {
        assertNotInitialized()
        if (!props[ConfigKeys.EXECUTORS]) {
            props["executors"] = []
        }
        props[ConfigKeys.EXECUTORS] << executorConfigs
        return this
    }

    ConfigBuilder doCacheGeneratedQueries() {
        assertNotInitialized()
        if (!props[ConfigKeys.CACHING]) {
            props[ConfigKeys.CACHING] = [:]
        }
        props[ConfigKeys.CACHING].generatedQueries = true
        return this
    }

    ConfigBuilder doCacheCompiledScripts() {
        assertNotInitialized()
        if (!props[ConfigKeys.CACHING]) {
            props[ConfigKeys.CACHING] = [:]
        }
        props[ConfigKeys.CACHING].compiledScripts = true
        return this
    }

    ConfigBuilder addDefaultImporters(String... clzFullName) {
        assertNotInitialized()
        clzFullName.each { addDefaultImporter(it) }
        return this
    }

    ConfigBuilder addDefaultImporters(Collection<String> clzFullName) {
        assertNotInitialized()
        clzFullName.each { addDefaultImporter(it) }
        return this
    }

    ConfigBuilder addDefaultImporter(String clzFullName) {
        assertNotInitialized()
        if (!props[ConfigKeys.DEFAULT_IMPORTS]) {
            props["defaultImports"] = []
        }
        props[ConfigKeys.DEFAULT_IMPORTS] << clzFullName
        return this
    }

    ConfigBuilder addTranslators(Collection<String> clzNamesList) {
        clzNamesList.each { addTranslator(it) }
        return this
    }

    ConfigBuilder addTranslator(String fullClzName) {
        assertNotInitialized()
        props[ConfigKeys.TRANSLATORS] ? props[ConfigKeys.TRANSLATORS].add(fullClzName) : [fullClzName]
        return this
    }

    ConfigBuilder activateDb(String dbImplName) {
        assertNotInitialized()
        props[ConfigKeys.ACTIVATE_DB] = dbImplName
        return this
    }

    private void assertNotInitialized() {
        synchronized (lock) {
            if (hasInitialized) {
                throw new NyConfigurationException("NyQL already has configured! You can't change after initialization! Sorry.")
            }
        }
    }

    ConfigBuilder setupFrom(Map map) {
        props.putAll(map)
        return this
    }

    Configurations build() {
        props[ConfigKeys.REPO_MAP] = repositoryMap
        props[ConfigKeys.SCRIPT_MAP] = scriptMapper

        synchronized (lock) {
            hasInitialized = true
        }
        Configurations.instance().configure(props)
    }

    static ConfigBuilder instance() {
        return Holder.INSTANCE
    }

    private static class Holder {
        private static final ConfigBuilder INSTANCE = new ConfigBuilder()
    }

}
