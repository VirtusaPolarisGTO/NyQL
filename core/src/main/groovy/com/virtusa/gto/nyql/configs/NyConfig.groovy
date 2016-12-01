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
     * Sets the folder containing all the available scripts for NyQL.
     *
     * @param folder folder instance. This must be exist.
     * @return this config instance.
     */
    NyConfig scriptFolder(File folder) {
        configBuilder.addRepository([
                name: 'default',
                repo: 'com.virtusa.gto.nyql.engine.repo.QRepositoryImpl',
                mapper: 'com.virtusa.gto.nyql.engine.repo.QScriptsFolder',
                mapperArgs: [
                    baseDir: folder.getAbsolutePath()
                ]
        ])
        configBuilder.havingDefaultRepository('default')
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
        configBuilder.addExecutor([
                name: 'jdbc',
                factory: 'com.virtusa.gto.nyql.engine.impl.QJdbcExecutorFactory',
                url: jdbcUrl,
                username: jdbcUserName,
                password: jdbcPassword,

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
        ])
        this
    }

    /**
     * Creates a new configuration instance which can be passed to create a NyQLInstance.
     *
     * @return built configuration instance.
     */
    Configurations build() {
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

}
