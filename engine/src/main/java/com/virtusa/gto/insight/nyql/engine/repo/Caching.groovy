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
        LOGGER.debug("Compiling all dsl scripts...")
        for (QSource qSource : sources) {
            try {
                gcl.parseClass(qSource.codeSource, true)
            } catch (Throwable ex) {
                throw new NyException("Compilation error in script ${qSource.id}!", ex)
            }
        }
        LOGGER.debug("Done.")
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
        def clazz
        if (Configurations.instance().cacheRawScripts()) {
            clazz = gcl.parseClass(sourceScript.codeSource, true)
        } else {
            clazz = gcl.parseClass(sourceScript.file)
        }
        Script scr = clazz.newInstance() as Script
        scr.setBinding(new Binding(session?.sessionVariables ?: [:]))
        return scr
    }

    void clearGeneratedCache() {
        cache.clear()
    }

    void invalidateGeneratedCache(String scriptId) {
        cache.remove(scriptId)
    }

    private CompilerConfiguration makeCompilerConfigs() {
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
