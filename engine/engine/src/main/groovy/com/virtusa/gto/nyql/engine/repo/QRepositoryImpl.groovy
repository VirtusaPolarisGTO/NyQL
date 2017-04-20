package com.virtusa.gto.nyql.engine.repo

import com.virtusa.gto.nyql.QResultProxy
import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.engine.exceptions.NyScriptExecutionException
import com.virtusa.gto.nyql.engine.exceptions.NyScriptParseException
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.*
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Field
import java.nio.file.Paths

/**
 * @author IWEERARATHNA
 */
class QRepositoryImpl implements QRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(QRepositoryImpl)

    protected final QScriptMapper mapper

    protected final Caching caching

    protected Configurations configurations

    QRepositoryImpl(Configurations theConfigs, QScriptMapper scriptMapper) {
        caching = new Caching(theConfigs)
        mapper = scriptMapper
        configurations = theConfigs

        initCache()
    }

    private void initCache() throws NyException {
        if (mapper.canCacheAtStartup()) {
            caching.compileAllScripts(mapper.allSources())
        }
    }

    void clearCache(int level) {
        caching.clearGeneratedCache(level)
        LOGGER.warn('All caches cleared in query repository!')
    }

    QScript parse(String scriptIdGiven, QSession session) throws NyException {
        String scriptId = resolveScriptId(scriptIdGiven, session.currentCallingFromScript())
        QSource src = mapper.map(scriptId)

        if (configurations.cacheGeneratedQueries() && caching.hasGeneratedQuery(scriptId)) {
            LOGGER.trace('Script {} served from query cache.', scriptId)
            return caching.getGeneratedQuery(scriptId, session)
        }

        try {
            Script compiledScript = caching.getCompiledScript(src, session)

            LOGGER.info("Running script '{}'", scriptId)
            Object res = compiledScript.run()

            QScript script = convertResult(scriptId, res, session)
            cacheIfSpecified(compiledScript, scriptId, script)
            return script

        } catch (CompilationFailedException ex) {
            throw new NyScriptParseException(scriptId, src.id, ex)
        } catch (IOException ex) {
            throw new NyScriptExecutionException(scriptId, src.id, ex)
        }
    }

    @CompileStatic
    private static String resolveScriptId(String scriptId, String currentScript) {
        if (scriptId.startsWith('@')) {
            Paths.get(currentScript).resolve('..').resolve(scriptId.substring(1)).normalize().toString()
        } else {
            scriptId;
        }
    }

    protected void cacheIfSpecified(Script compiledScript, String scriptId, QScript script) {
        try {
            Field field = compiledScript.getClass().getDeclaredField(configurations.cachingIndicatorVarName())
            field.setAccessible(true)
            boolean doCache = field.get(compiledScript) ?: false
            if (doCache) {
                LOGGER.trace("Script $scriptId cachable status: " + doCache)
                caching.addGeneratedQuery(scriptId, script)
            }
        } catch (ignored) {
            //LOGGER.error("No field do_cache in $scriptId")
        }
    }

    protected static QScript convertResult(String scriptId, Object res, QSession session) {
        if (res instanceof QResultProxy) {
            return new QScript(id: scriptId, proxy: (QResultProxy) res, qSession: session)
        } else if (res instanceof QScriptList) {
            res.id = scriptId
            return res
        } else {
            new QScriptResult(id: scriptId, qSession: session, scriptResult: res)
        }
    }

    @Override
    void close() throws IOException {
        if (caching != null) {
            caching.close()
        }
    }
}
