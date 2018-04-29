package com.virtusa.gto.nyql.configs

import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.model.QScriptMapper
import groovy.transform.CompileStatic

/**
 * @author iweerarathna
 */
class NyConfigV2 extends NyConfig {

    private final Map executor = [
            impl: 'jdbc',
            url: '',
            username: '',
            password: '',
            jdbcDriverClass: null,
            jdbcDataSourceClass: null,

            pooling: [
                    impl: 'hikari',
                    maximumPoolSize: 1,
                    prepStmtCacheSize: 300,
                    prepStmtCacheSqlLimit: 2048,
                    useServerPrepStmts: true,
                    connectionTimeout: 30000,
                    idleTimeout: 0,
                    maxLifetime: 0
            ]
    ]


    protected NyConfigV2(ConfigBuilder configBuilder) {
        super(configBuilder)
    }

    @Override
    NyConfig jdbcPooling(Map hikariPoolConfigs) {
        Map temp = new HashMap(hikariPoolConfigs)
        temp.impl = 'hikari'
        executor.pooling = temp
        this
    }

    @Override
    NyConfig jdbcOptions(String jdbcUrl, String jdbcUserName, String jdbcPassword, String jdbcDriverClz, String jdbcDataSourceClz) {
        executor.url = jdbcUrl
        executor.username = jdbcUserName
        executor.password = jdbcPassword
        this
    }

    @Override
    NyConfig scriptFolder(File folder) throws NyConfigurationException {
        return super.scriptFolder(folder)
    }

    @Override
    NyConfig scriptFolder(File folder, String exclusions) throws NyConfigurationException {
        return super.scriptFolder(folder, exclusions)
    }

    @Override
    NyConfig scriptFolder(File folder, String exclusions, String inclusions) throws NyConfigurationException {
        assertMapperSetup()

        configBuilder.setTheRepository([
                mapper: 'folder',
                mapperArgs: [
                        baseDir: folder.getAbsolutePath(),
                        inclusions: inclusions,
                        exclusions: exclusions
                ]
        ])
        mapperAdded = true
        this
    }

    @Override
    NyConfig scriptFolders(Collection<File> folders) throws NyConfigurationException {
        assertMapperSetup()

        List<String> fpaths = new LinkedList<>()
        for (File file : folders) {
            fpaths.add(file.getAbsolutePath())
        }
        configBuilder.setTheRepository([
                mapper: 'folders',
                mapperArgs: [
                        baseDirs: fpaths
                ]
        ])
        mapperAdded = true
        this
    }

    @Override
    NyConfig withCustomScriptMapper(Class<? extends QScriptMapper> mapperClz, Map configs) {
        assertMapperSetup()

        configBuilder.setTheRepository([
                mapper: mapperClz.name,
                mapperArgs: configs
        ])
        mapperAdded = true
        this
    }

    @CompileStatic
    @Override
    Configurations build() throws NyConfigurationException {
        configBuilder.setTheExecutor(executor)
        configBuilder.build()
    }
}
