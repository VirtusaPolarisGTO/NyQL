package com.virtusa.gto.insight.nyql.engine.repo

import com.virtusa.gto.insight.nyql.configs.Configurations
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QSession
import com.virtusa.gto.insight.nyql.model.QSource
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap

/**
 * @author IWEERARATHNA
 */
class Caching implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Caching.class)

    private final Map<String, QScript> cache = new ConcurrentHashMap<>()

    private CompilerConfiguration compilerConfigurations
    private final GroovyClassLoader gcl

    Caching() {
        gcl = new GroovyClassLoader(Thread.currentThread().contextClassLoader, makeCompilerConfigs())
    }

    void compileAllScripts(Collection<QSource> sources) throws NyException {
        if (Configurations.instance().cacheRawScripts()) {
            LOGGER.debug("Compiling all ${sources.size()} dsl script(s)...")
            for (QSource qSource : sources) {
                String id = qSource.id
                try {
                    LOGGER.debug('  Compiling: ' + id)
                    gcl.parseClass(qSource.codeSource, true)
                } catch (CompilationFailedException ex) {
                    LOGGER.error("Compilation error in script '$id'", ex)
                    throw new NyException("Compilation error in script '$id'!", ex)
                }
            }
            LOGGER.debug('Compilation successful!')
        }
    }

    boolean hasGeneratedQuery(String scriptId) {
        return cache.containsKey(scriptId)
    }

    QScript getGeneratedQuery(String scriptId, QSession session) {
        QScript qScript = cache.get(scriptId)
        if (qScript != null) {
            return new QScript(id: qScript.id, proxy: qScript.proxy, qSession: session)
        }
        return qScript
    }

    private static QScript spawnScriptFrom(QScript src) {
        QScript script = new QScript(id: src.id, qSession: null)
        def resultProxy = src.proxy
        if (resultProxy != null) {
            script.proxy = resultProxy.dehydrate()
        }
        script
    }

    QScript addGeneratedQuery(String scriptId, QScript script) {
        cache.put(scriptId, spawnScriptFrom(script))
        return script
    }

    Script getCompiledScript(QSource sourceScript, QSession session) {
        Binding binding = new Binding(session?.sessionVariables ?: [:])
        if (Configurations.instance().cacheRawScripts()) {
            def clazz = gcl.parseClass(sourceScript.codeSource, true)
            Script scr = clazz.newInstance() as Script
            scr.setBinding(binding)
            return scr
        } else {
            GroovyShell shell = new GroovyShell(binding, makeCompilerConfigs())
            return shell.parse(sourceScript.file)
        }
    }

    void clearGeneratedCache(int level) {
        if (level >= 0) {
            cache.clear()
        }
    }

    void invalidateGeneratedCache(String scriptId) {
        cache.remove(scriptId)
    }

    CompilerConfiguration makeCompilerConfigs() {
        if (compilerConfigurations != null) {
            return compilerConfigurations
        }

        compilerConfigurations = new CompilerConfiguration()

        String[] defImports = Configurations.instance().defaultImports()
        if (defImports != null) {
            ImportCustomizer importCustomizer = new ImportCustomizer()
            importCustomizer.addImports(defImports)
            compilerConfigurations.addCompilationCustomizers(importCustomizer)
        }
        return compilerConfigurations
    }

    @Override
    void close() throws IOException {
        if (gcl != null) {
            gcl.close()
        }
    }
}
