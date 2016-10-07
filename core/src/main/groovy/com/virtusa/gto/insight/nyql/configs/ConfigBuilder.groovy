package com.virtusa.gto.insight.nyql.configs

import com.virtusa.gto.insight.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.insight.nyql.model.QRepository
import com.virtusa.gto.insight.nyql.model.QScriptMapper
import groovy.transform.PackageScope

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

    public ConfigBuilder havingDefaultRepository(String repoName) {
        assertNotInitialized()
        props["defaultRepository"] = repoName
        return this
    }

    public ConfigBuilder addScriptLoader(String mapperName, QScriptMapper qScriptMapper) {
        scriptMapper.put(mapperName, qScriptMapper)
        return this
    }

    public QScriptMapper getScriptLoader(String mapperName) {
        return scriptMapper.get(mapperName)
    }

    public QRepository getRepository(String name) {
        return repositoryMap[name]
    }

    public ConfigBuilder addRepository(String name, QRepository repository) {
        repositoryMap.put(name, repository)
        return this
    }

    public ConfigBuilder addRepository(Map repositoryConfig) {
        assertNotInitialized()
        if (!props["repositories"]) {
            props["repositories"] = []
        }
        props["repositories"] << repositoryConfig
        return this
    }

    public ConfigBuilder havingDefaultExecutor(String executorName) {
        assertNotInitialized()
        props["defaultExecutor"] = executorName
        return this
    }

    public ConfigBuilder addExecutor(Map executorConfigs) {
        assertNotInitialized()
        if (!props["executors"]) {
            props["executors"] = []
        }
        props["executors"] << executorConfigs
        return this
    }

    public ConfigBuilder doCacheGeneratedQueries() {
        assertNotInitialized()
        if (!props["caching"]) {
            props["caching"] = [:]
        }
        props["caching"].generatedQueries = true
        return this
    }

    public ConfigBuilder doCacheCompiledScripts() {
        assertNotInitialized()
        if (!props["caching"]) {
            props["caching"] = [:]
        }
        props["caching"].compiledScripts = true
        return this
    }

    public ConfigBuilder addDefaultImporters(String... clzFullName) {
        assertNotInitialized()
        clzFullName.each { addDefaultImporter(it) }
        return this
    }

    public ConfigBuilder addDefaultImporters(Collection<String> clzFullName) {
        assertNotInitialized()
        clzFullName.each { addDefaultImporter(it) }
        return this
    }

    public ConfigBuilder addDefaultImporter(String clzFullName) {
        assertNotInitialized()
        if (!props["defaultImports"]) {
            props["defaultImports"] = []
        }
        props["defaultImports"] << clzFullName
        return this
    }

    public ConfigBuilder addTranslators(Collection<String> clzNamesList) {
        clzNamesList.each { addTranslator(it) }
        return this
    }

    public ConfigBuilder addTranslator(String fullClzName) {
        assertNotInitialized()
        props["translators"] ? props["translators"].add(fullClzName) : [fullClzName]
        return this
    }

    public ConfigBuilder activateDb(String dbImplName) {
        assertNotInitialized()
        props["activate"] = dbImplName
        return this
    }

    private void assertNotInitialized() {
        synchronized (lock) {
            if (hasInitialized) {
                throw new NyConfigurationException("NyQL already has configured! You can't change after initialization! Sorry.")
            }
        }
    }

    public ConfigBuilder setupFrom(Map map) {
        props.putAll(map)
        return this
    }

    public Configurations build() {
        props["__repoMap"] = repositoryMap
        props["__scriptMapper"] = scriptMapper

        synchronized (lock) {
            hasInitialized = true
        }
        Configurations.instance().configure(props)
    }

    public static ConfigBuilder instance() {
        return Holder.INSTANCE
    }

    private static class Holder {
        private static final ConfigBuilder INSTANCE = new ConfigBuilder()
    }

}
