package com.virtusa.gto.nyql.configs

import com.virtusa.gto.nyql.db.QDbFactory
import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.DbInfo
import com.virtusa.gto.nyql.model.QDatabaseRegistry
import com.virtusa.gto.nyql.model.QExecutorFactory
import com.virtusa.gto.nyql.model.QExecutorRegistry
import com.virtusa.gto.nyql.model.QRepository
import com.virtusa.gto.nyql.model.QRepositoryRegistry
import com.virtusa.gto.nyql.model.QScriptMapper
import com.virtusa.gto.nyql.model.impl.QProfExecutorFactory
import com.virtusa.gto.nyql.model.impl.QProfRepository
import com.virtusa.gto.nyql.utils.Constants
import com.virtusa.gto.nyql.utils.ReflectUtils
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 * @author iweerarathna
 */
class ConfigurationsV2 extends Configurations {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationsV2)

    ConfigurationsV2() {
        super()
    }

    @Override
    protected void doConfig() throws NyException {
        databaseRegistry = QDatabaseRegistry.newInstance().discover(super.classLoader)
        executorRegistry = QExecutorRegistry.newInstance()
        repositoryRegistry = QRepositoryRegistry.newInstance()

        // load query related configurations
        loadQueryInfo(getQueryConfigs())

        boolean profileEnabled = startProfiler()

        // mark active database
        String activeDb = getActivatedDb()
        LOGGER.info("Activated: ${activeDb}")
        checkTranslators(activeDb)

        // load repositories
        loadRepos(profileEnabled)

        // load executors
        DbInfo dbInfo = loadExecutors(activeDb, profileEnabled)

        // finally, initialize factory
        def factory = databaseRegistry.getDbFactory(activeDb)
        factory.init(this, dbInfo)
    }

    protected void checkTranslators(String activeDb) {
        if (activeDb == null) {
            throw new NyConfigurationException('No database has been specified to be activated!')
        }

        def allDbs = databaseRegistry.listAll()
        LOGGER.info("Found database implementations in classpath: [ ${allDbs.join(', ')} ]")
    }

    @CompileStatic
    @Override
    protected DbInfo loadExecutors(String activeDb, boolean profEnabled) {
        QDbFactory activeFactory = databaseRegistry.getDbFactory(activeDb)
        Map executor = properties.get(ConfigKeys.EXECUTOR) as Map

        String execName = executor.get('name')
        String execImpl = executor.get('impl')

        executor.putIfAbsent(ConfigKeys.JDBC_DRIVER_CLASS_KEY, activeFactory.driverClassName())
        executor.putIfAbsent(ConfigKeys.JDBC_DATASOURCE_CLASS_KEY, activeFactory.dataSourceClassName())

        QExecutorFactory executorFactory = (QExecutorFactory) ReflectUtils.newInstance(execImpl, classLoader, executor)

        if (profEnabled) {
            executorFactory = new QProfExecutorFactory(this, executorFactory)
        }
        DbInfo activeDbInfo = executorFactory.init(executor, this)
        executorRegistry.register(execName, executorFactory)

        activeDbInfo
    }

    @Override
    @CompileStatic
    protected void loadRepos(boolean profEnabled) {
        Map repository = properties.get(ConfigKeys.REPOSITORY) as Map
        checkRepository(repository)

        String repoName = repository.get('name') ?: Constants.DEFAULT_REPOSITORY_NAME
        String repoImpl = repository.get('impl') ?: Constants.DEFAULT_REPOSITORY_IMPL
        String mapper = repository.get('mapper')

        Map args = (repository.get('mapperArgs') ?: [:]) as Map
        args.put(ConfigKeys.LOCATION_KEY, properties._location)

        // @TODO call from mapper factory
        QScriptMapper scriptMapper = (QScriptMapper) ReflectUtils.callStaticMethod(mapper, classLoader, args)
        QRepository qRepository = (QRepository) ReflectUtils.newInstance(repoImpl, classLoader, this, scriptMapper)

        if (profEnabled) {
            qRepository = new QProfRepository(this, qRepository)
        }
        repositoryRegistry.register(repoName, qRepository)

        if (properties[ConfigKeys.REPO_MAP]) {
            Map<String, QRepository> repositoryMap = (Map<String, QRepository>) properties[ConfigKeys.REPO_MAP]
            repositoryMap.each {
                QRepository tmpRepo = profEnabled ? new QProfRepository(this, it.value) : it.value
                repositoryRegistry.register(it.key, tmpRepo)
            }
        }
    }

    private static void checkRepository(Map repo) {
        if (!repo.mapper) {
            throw new NyException('Mandatory field "mapper" is not specified!')
        }
    }
}
