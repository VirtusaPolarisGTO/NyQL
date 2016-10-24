package com.virtusa.gto.insight.nyql.engine.repo

import com.virtusa.gto.insight.nyql.configs.Configurations
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.NyBaseScript
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QSession
import com.virtusa.gto.insight.nyql.model.QSource
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SourceAwareCustomizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap
/**
 * @author IWEERARATHNA
 */
@CompileStatic
class Caching implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Caching)

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
        cache.containsKey(scriptId)
    }

    QScript getGeneratedQuery(String scriptId, QSession session) {
        QScript qScript = cache.get(scriptId)
        if (qScript != null) {
            return new QScript(id: qScript.id, proxy: qScript.proxy, qSession: session)
        }
        qScript
    }

    /**
     * Spawn a new script instance from already cached instance of script.
     *
     * @param src source script to make a clone.
     * @return new instance of script.
     */
    private static QScript spawnScriptFrom(QScript src) {
        src.spawn()
    }

    /**
     * Add a generated query to the cache.
     *
     * @param scriptId script id.
     * @param script generated query instance with result.
     * @return the added script instance.
     */
    QScript addGeneratedQuery(String scriptId, QScript script) {
        cache.put(scriptId, spawnScriptFrom(script))
        script
    }

    /**
     * Returns a new instance of compiled script from the cache.
     *
     * @param sourceScript corresponding source of the script.
     * @param session session instance.
     * @return newly created script instance.
     */
    Script getCompiledScript(QSource sourceScript, QSession session) {
        Binding binding = new Binding(session?.sessionVariables ?: [:])
        if (Configurations.instance().cacheRawScripts()) {
            def clazz = gcl.parseClass(sourceScript.codeSource, true)
            NyBaseScript scr = clazz.newInstance() as NyBaseScript
            scr.setBinding(binding)
            scr.setSession(session)
            scr
        } else {
            GroovyShell shell = new GroovyShell(binding, makeCompilerConfigs())
            Script parsedScript = shell.parse(sourceScript.file) as NyBaseScript
            parsedScript.setSession(session)
            parsedScript
        }
    }

    /**
     * CLear the generated query cache or class loader cache.
     *
     * @param level level of cache to clean.
     */
    void clearGeneratedCache(int level) {
        if (level >= 0) {
            cache.clear()
        }
        if (level > 1) {
            gcl.clearCache()
        }
    }

    /**
     * Create a set of configurations requires for script initial compilation.
     *
     * @return compiler configuration instance newly created or already created.
     */
    CompilerConfiguration makeCompilerConfigs() {
        if (compilerConfigurations != null) {
            return compilerConfigurations
        }

        compilerConfigurations = new CompilerConfiguration()

        compilerConfigurations.scriptBaseClass = NyBaseScript.class.name
        ASTTransformationCustomizer astStatic = new ASTTransformationCustomizer(CompileStatic)
        SourceAwareCustomizer sac = new SourceAwareCustomizer(astStatic)
        sac.extensionValidator = { ext -> ext == 'sgroovy' }
        compilerConfigurations.addCompilationCustomizers(sac)

        String[] defImports = Configurations.instance().defaultImports()
        if (defImports != null) {
            ImportCustomizer importCustomizer = new ImportCustomizer()
            importCustomizer.addImports(defImports)
            compilerConfigurations.addCompilationCustomizers(importCustomizer)
        }
        compilerConfigurations
    }

    @Override
    void close() throws IOException {
        if (gcl != null) {
            gcl.close()
        }
    }
}
