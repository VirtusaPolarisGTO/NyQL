package com.virtusa.gto.insight.nyql.engine.repo

import com.virtusa.gto.insight.nyql.configs.Configurations
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QSession
import com.virtusa.gto.insight.nyql.model.QSource
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

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
                try {
                    LOGGER.debug("  Compiling: " + qSource.id)
                    gcl.parseClass(qSource.codeSource, true)
                } catch (Throwable ex) {
                    throw new NyException("Compilation error in script ${qSource.id}!", ex)
                }
            }
            LOGGER.debug("Compilation successful!")
        }
    }

    boolean hasGeneratedQuery(String scriptId) {
        return cache.containsKey(scriptId)
    }

    QScript getGeneratedQuery(String scriptId, QSession session) {
        QScript qScript = cache.get(scriptId)
        if (qScript != null) {
            return new QScript(proxy: qScript.proxy, qSession: session)
        }
        return qScript
    }

    QScript addGeneratedQuery(String scriptId, QScript script) {
        cache.put(scriptId, script)
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

    void clearGeneratedCache() {
        cache.clear()
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
