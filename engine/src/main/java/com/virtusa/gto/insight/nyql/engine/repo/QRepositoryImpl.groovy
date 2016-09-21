package com.virtusa.gto.insight.nyql.engine.repo

import com.virtusa.gto.insight.nyql.QResultProxy
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.*
import com.virtusa.gto.insight.nyql.engine.exceptions.NyScriptExecutionException
import com.virtusa.gto.insight.nyql.engine.exceptions.NyScriptNotFoundException
import com.virtusa.gto.insight.nyql.engine.exceptions.NyScriptParseException
import org.codehaus.groovy.control.CompilationFailedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author IWEERARATHNA
 */
class QRepositoryImpl implements QRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(QRepositoryImpl.class)

    private final QScriptMapper mapper

    private final Map<String, QScript> cache = [:]

    private final Map<String, Script> gscriptMap = [:]

    QRepositoryImpl(QScriptMapper scriptMapper) {
        mapper = scriptMapper
    }

    public void clearCache(int level) {
        cache.clear()
        LOGGER.warn("All query repository cache cleared!")
    }

    QScript parse(String scriptId, QSession session) throws NyException {
        QSource src = mapper.map(scriptId)
        if (src == null || src.file == null || !src.file.exists()) {
            throw new NyScriptNotFoundException(scriptId)
        }

        if (src.doCache && cache.containsKey(scriptId)) {
            LOGGER.trace("Script {} served from query cache.", scriptId)
            return cache.get(scriptId)
        }

        Binding binding = new Binding(session?.sessionVariables ?: new HashMap<>())
        GroovyShell shell = new GroovyShell(binding)

        try {
            Script compiledScript = gscriptMap.computeIfAbsent(scriptId, { id ->
                        LOGGER.info("Compiling script {}", src.file.absolutePath)
                        return shell.parse(src.file)
                    })

            if (LOGGER.isInfoEnabled()) LOGGER.info("Running script '{}'", scriptId)
            Object res = compiledScript.run()

            QScript script
            if (res instanceof QResultProxy) {
                script = new QScript(proxy: (QResultProxy) res, qSession: session)
            } else {
                script = new QScriptResult(qSession: session, scriptResult: res)
            }
            if (src.isDoCache()) {
                cache.put(scriptId, script)
            }
            return script

        } catch (CompilationFailedException ex) {
            throw new NyScriptParseException(scriptId, src.file, ex)
        } catch (IOException ex) {
            throw new NyScriptExecutionException(scriptId, src.file, ex)
        }
    }

}
