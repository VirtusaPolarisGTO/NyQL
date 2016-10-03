package com.virtusa.gto.insight.nyql.engine.repo

import com.virtusa.gto.insight.nyql.QResultProxy
import com.virtusa.gto.insight.nyql.configs.Configurations
import com.virtusa.gto.insight.nyql.engine.exceptions.NyScriptExecutionException
import com.virtusa.gto.insight.nyql.engine.exceptions.NyScriptNotFoundException
import com.virtusa.gto.insight.nyql.engine.exceptions.NyScriptParseException
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.*
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author IWEERARATHNA
 */
class QRepositoryImpl implements QRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(QRepositoryImpl.class)

    private final QScriptMapper mapper

    private final Caching caching

    private Configurations configurations

    QRepositoryImpl(QScriptMapper scriptMapper) {
        caching = new Caching()
        mapper = scriptMapper
        configurations = Configurations.instance()

        initCache()
    }

    private void initCache() throws NyException {
        caching.compileAllScripts(mapper.allSources())
    }

    public void clearCache(int level) {
        caching.clearGeneratedCache()
        LOGGER.warn("All query repository cache cleared!")
    }

    QScript parse(String scriptId, QSession session) throws NyException {
        QSource src = mapper.map(scriptId)
        if (src == null || src.file == null || !src.file.exists()) {
            throw new NyScriptNotFoundException(scriptId)
        }

        if (Configurations.instance().cacheGeneratedQueries() && caching.hasGeneratedQuery(scriptId)) {
            LOGGER.trace("Script {} served from query cache.", scriptId)
            return caching.getGeneratedQuery(scriptId, session)
        }

        try {
            Script compiledScript = caching.getCompiledScript(src, session)

            LOGGER.info("Running script '{}'", scriptId)
            Object res = compiledScript.run()

            QScript script = convertResult(res, session)
            cacheIfSpecified(compiledScript, scriptId, script)
            return script

        } catch (CompilationFailedException ex) {
            throw new NyScriptParseException(scriptId, src.file, ex)
        } catch (IOException ex) {
            throw new NyScriptExecutionException(scriptId, src.file, ex)
        }
    }

    private void cacheIfSpecified(Script compiledScript, String scriptId, QScript script) {
        if (compiledScript.getBinding().hasVariable(configurations.cachingIndicatorVarName())) {
            boolean doCache = (compiledScript.getBinding().variables.get(configurations.cachingIndicatorVarName()) ?: false)
            if (doCache) {
                LOGGER.trace("Script $scriptId cachable status: " + doCache)
                caching.addGeneratedQuery(scriptId, script)
            }
        }
    }

    private static QScript convertResult(Object res, QSession session) {
        if (res instanceof QResultProxy) {
            return new QScript(proxy: (QResultProxy) res, qSession: session)
        } else if (res instanceof QScriptList) {
            return res
        } else {
            return new QScriptResult(qSession: session, scriptResult: res)
        }
    }

    @Override
    void close() throws IOException {
        if (caching != null) {
            caching.close()
        }
    }
}
