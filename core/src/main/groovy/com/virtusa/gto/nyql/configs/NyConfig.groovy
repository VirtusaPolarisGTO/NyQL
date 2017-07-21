/*
 * Copyright (C) 2016. VirtusaPolaris - Global Technology Office.
 * (http://www.virtusapolaris.com/eraplatform) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.virtusa.gto.nyql.configs

import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.model.QScriptMapper
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import java.nio.charset.StandardCharsets
/**
 * Easy configuration builder class when users prefer to do the configurations
 * programmatically.
 *
 * @author IWEERARATHNA
 */
@CompileStatic
final class NyConfig {

    private static final String DEF_CONFIG_PATH = 'com/virtusa/gto/nyql/configs/default-config.json'

    private final ConfigBuilder configBuilder

    private boolean mapperAdded = false
    private final Map executor = [
            name: 'jdbc',
            factory: 'com.virtusa.gto.nyql.engine.impl.QJdbcExecutorFactory',
            url: '',
            username: '',
            password: '',
            jdbcDriverClass: null,
            jdbcDataSourceClass: null,

            pooling: [
                    impl: 'com.virtusa.gto.nyql.engine.pool.impl.QHikariPool',
                    maximumPoolSize: 1,
                    prepStmtCacheSize: 300,
                    prepStmtCacheSqlLimit: 2048,
                    useServerPrepStmts: true,
                    connectionTimeout: 30000,
                    idleTimeout: 0,
                    maxLifetime: 0
            ]
    ]

    private NyConfig(ConfigBuilder configBuilder) {
        this.configBuilder = configBuilder
    }

    /**
     * Sets the active database for query parsing and execution.
     *
     * @param dbName database name. (either mysql, pg, or mssql)
     * @return this config instance.
     */
    NyConfig forDatabase(String dbName) {
        configBuilder.activateDb(dbName)
        this
    }

    /**
     * Sets the active database for query parsing and execution.
     *
     * @param dbName database name. (either mysql, pg, or mssql)
     * @param dbTranslatorClass associate translator class for the database.
     * @return this config instance.
     */
    NyConfig forDatabase(String dbName, Class<?> dbTranslatorClass) {
        forDatabase(dbName)
        configBuilder.addTranslator(dbTranslatorClass.name)
        this
    }

    /**
     * Setup caching options. When not specified, below values will be applied.
     *     - compileScripts: true
     *     - generatedQueries: true
     *     - allowRecompile: false
     *
     * @param compileScripts to compile scripts at initialization. (default: true)
     * @param generatedQueries to cache generated queries if specified in script. (default: true)
     * @param allowRecompile allow to recompile a script without restarting NyQL. (default: false)
     * @return this config instance.
     */
    NyConfig withCaching(boolean compileScripts = true, boolean generatedQueries = true, boolean allowRecompile = false) {
        configBuilder.doCacheCompiledScripts(compileScripts)
            .doCacheGeneratedQueries(generatedQueries)
            .doCacheAllowRecompilation(allowRecompile)
        this
    }

    /**
     * Sets the folder containing all the available scripts for NyQL.
     *
     * @param folder folder instance. This must be exist.
     * @param exclusions all exclusion patterns as comma separated string.
     * @param inclusions all inclusion patterns as comma separated string.
     * @return this config instance.
     */
    NyConfig scriptFolder(File folder, String exclusions = null, String inclusions = null) throws NyConfigurationException {
        assertMapperSetup()

        configBuilder.addRepository([
                name: 'default',
                repo: 'com.virtusa.gto.nyql.engine.repo.QRepositoryImpl',
                mapper: 'com.virtusa.gto.nyql.engine.repo.QScriptsFolder',
                mapperArgs: [
                    baseDir: folder.getAbsolutePath(),
                    inclusions: inclusions,
                    exclusions: exclusions
                ]
        ])
        configBuilder.havingDefaultRepository('default')
        mapperAdded = true
        this
    }

    /**
     * Sets the folders containing all the available scripts for NyQL.
     *
     * @param folders folder instances. These must be exist.
     * @return this config instance.
     */
    NyConfig scriptFolders(Collection<File> folders) throws NyConfigurationException {
        assertMapperSetup()

        List<String> fpaths = new LinkedList<>()
        for (File file : folders) {
            fpaths.add(file.getAbsolutePath())
        }
        configBuilder.addRepository([
                name: 'default',
                repo: 'com.virtusa.gto.nyql.engine.repo.QRepositoryImpl',
                mapper: 'com.virtusa.gto.nyql.engine.repo.QScriptFolders',
                mapperArgs: [
                    baseDirs: fpaths
                ]
        ])
        configBuilder.havingDefaultRepository('default')
        mapperAdded = true
        this
    }

    /**
     * Configure script loading mapper with custom class and configs.
     *
     * @param mapperClz mapper class.
     * @param configs configs for mapper.
     * @return this config instance.
     */
    NyConfig withCustomScriptMapper(Class<? extends QScriptMapper> mapperClz, Map configs) {
        assertMapperSetup()

        configBuilder.addRepository([
                name: 'default',
                repo: 'com.virtusa.gto.nyql.engine.repo.QRepositoryImpl',
                mapper: mapperClz.name,
                mapperArgs: configs
        ])
        configBuilder.havingDefaultRepository('default')
        mapperAdded = true
        this
    }

    /**
     * Sets the jdbc parameter for query executions.
     *
     * @param jdbcUrl jdbc url.
     * @param jdbcUserName user name for jdbc.
     * @param jdbcPassword password for jdbc.
     * @return this config instance.
     */
    NyConfig jdbcOptions(String jdbcUrl, String jdbcUserName, String jdbcPassword) {
        jdbcOptions(jdbcUrl, jdbcUserName, jdbcPassword, null, null)
    }

    /**
     * Sets the jdbc parameter for query executions.
     *
     * @param jdbcUrl jdbc url.
     * @param jdbcUserName user name for jdbc.
     * @param jdbcPassword password for jdbc.
     * @param jdbcDriverClz full driver class name.
     * @return this config instance.
     */
    NyConfig jdbcOptions(String jdbcUrl, String jdbcUserName, String jdbcPassword, String jdbcDriverClz) {
        jdbcOptions(jdbcUrl, jdbcUserName, jdbcPassword, jdbcDriverClz, null)
    }

    /**
     * Sets the jdbc parameter for query executions.
     *
     * @param jdbcUrl jdbc url.
     * @param jdbcUserName user name for jdbc.
     * @param jdbcPassword password for jdbc.
     * @param jdbcDriverClz full driver class name.
     * @param jdbcDataSourceClz full data source class name.
     * @return this config instance.
     */
    NyConfig jdbcOptions(String jdbcUrl, String jdbcUserName, String jdbcPassword,
                         String jdbcDriverClz, String jdbcDataSourceClz) {
        executor.url = jdbcUrl
        executor.username = jdbcUserName
        executor.password = jdbcPassword
        executor.jdbcDriverClass = jdbcDriverClz
        executor.jdbcDataSourceClass = jdbcDataSourceClz

        configBuilder.havingDefaultExecutor(executor.name)
        this
    }

    /**
     * Setup jdbc pool configurations using most important parameters.
     *
     * Refer Hikari JDBC pool configurations for parameter descriptions.
     *
     * @param maxPoolSize maximum pool size.
     * @param connectionTimeOut connection time out in milliseconds.
     * @param idleTimeOut idle time out in milliseconds.
     * @param maxLifeTime maximum life time in milliseconds.
     * @return this config instance.
     */
    NyConfig jdbcPooling(int maxPoolSize, long connectionTimeOut = 30000, long idleTimeOut = 0, long maxLifeTime = 0) {
        Map pool = (Map) executor.pooling

        pool.maximumPoolSize = maxPoolSize
        pool.connectionTimeout = connectionTimeOut
        pool.idleTimeout = idleTimeOut
        pool.maxLifetime = maxLifeTime
        this
    }

    /**
     * Setup jdbc pool configurations using custom pool parameters.
     *
     * Refer Hikari JDBC pool configurations before submitting parameters and use those
     * parameter names exactly as the map keys.
     *
     * @param hikariPoolConfigs pool configs as map
     * @return this config instance.
     */
    NyConfig jdbcPooling(Map hikariPoolConfigs) {
        Map temp = new HashMap(hikariPoolConfigs)
        temp.impl = 'com.virtusa.gto.nyql.engine.pool.impl.QHikariPool'
        executor.pooling = temp
        this
    }

    /**
     * Creates a new configuration instance which can be passed to create a NyQLInstance.
     *
     * @return built configuration instance.
     */
    Configurations build() throws NyConfigurationException {
        configBuilder.addExecutor(executor)
        configBuilder.build()
    }

    /**
     * Creates an easy configuration instance with default but minimum options.
     *
     * @return a new configuration instance.
     * @throws NyConfigurationException if no default configuration file is found in classpath.
     */
    static NyConfig withDefaults() throws NyConfigurationException {
        InputStream baseConf = Thread.currentThread().contextClassLoader.getResourceAsStream(DEF_CONFIG_PATH)
        if (baseConf != null) {
            Map defData = new JsonSlurper().parse(baseConf, StandardCharsets.UTF_8.name()) as Map
            ConfigBuilder cb = new ConfigBuilder().setupFrom(defData)
            return new NyConfig(cb)
        }
        throw new NyConfigurationException('No default configuration file found in classpath!')
    }

    private void assertMapperSetup() throws NyConfigurationException {
        if (mapperAdded) {
            throw new NyConfigurationException("Cannot add more than one script mapper to configurations!")
        }
    }

}
