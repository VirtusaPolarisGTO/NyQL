package com.virtusa.gto.insight.nyql.configs

import com.virtusa.gto.insight.nyql.DSLContext
import com.virtusa.gto.insight.nyql.db.QDbFactory
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.QDatabaseRegistry
import com.virtusa.gto.insight.nyql.model.QRepository
import com.virtusa.gto.insight.nyql.model.QRepositoryRegistry
import com.virtusa.gto.insight.nyql.model.QScriptMapper
import com.virtusa.gto.insight.nyql.utils.Constants
import com.virtusa.gto.insight.nyql.utils.QUtils
import org.apache.commons.lang3.BooleanUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author IWEERARATHNA
 */
class Configurations {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configurations.class)

    private Map properties = [:]

    private String cacheVarName

    private final Object lock = new Object();
    private boolean configured = false

    private Configurations() {}

    Configurations configure(Map configProps) throws NyException {
        properties = configProps

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
        List<String> clzNames = getAvailableTranslators()
        if (QUtils.notNullNorEmpty(clzNames)) {
            clzNames.each {
                try {
                    def factory = Class.forName(it).newInstance() as QDbFactory
                    QDatabaseRegistry.instance.register(factory)
                } catch (ClassNotFoundException ex) {
                    throw new NyException("No database implementation class found by name '$it'!", ex)
                }
            }
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
        String defRepo = properties.defaultRepository ?: "default"
        List repos = properties.repositories ?: []
        for (Map r : repos) {
            Map args = r.mapperArgs ?: [:]
            boolean thisDef = r.name == defRepo
            QScriptMapper scriptMapper = Class.forName(r.mapper).createNew(args)
            QRepository qRepository = (QRepository) Class.forName(r.repo).newInstance([scriptMapper].toArray())

            QRepositoryRegistry.getInstance().register(r.name, qRepository, thisDef)
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

    List<String> getAvailableTranslators() {
        return properties.translators
    }

    static Configurations instance() {
        return Holder.INSTANCE
    }

    private static class Holder {
        private static final Configurations INSTANCE = new Configurations()
    }

}
