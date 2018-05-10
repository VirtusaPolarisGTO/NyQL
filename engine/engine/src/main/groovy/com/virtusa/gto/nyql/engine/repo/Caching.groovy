package com.virtusa.gto.nyql.engine.repo

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.*
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
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
    private final NyGroovyClassLoader gcl
    private final Configurations configurations
    private final QScriptMapper mapper
    private final Object clzLoaderLock = new Object()

    Caching(Configurations theConfigs, QScriptMapper scriptMapper) {
        configurations = theConfigs
        mapper = scriptMapper

        gcl = new NyGroovyClassLoader(Thread.currentThread().contextClassLoader, makeCompilerConfigs())
    }

    void compileAllScripts(Collection<QSource> sources) throws NyException {
        if (configurations.cacheRawScripts()) {
            synchronized (clzLoaderLock) {
                int n = sources.size()
                int len = String.valueOf(n).length()
                int curr = 1

                LOGGER.info("Compiling all ${n} dsl script(s)...")
                for (QSource qSource : sources) {
                    String id = qSource.id
                    try {
                        LOGGER.debug('  Compiling [' + padLeft(len, curr++) + '/' + n + ']: ' + id)
                        gcl.parseClass(qSource.codeSource, true)
                    } catch (CompilationFailedException ex) {
                        LOGGER.error("Compilation error in script '$id'", ex)
                        throw new NyException("Compilation error in script '$id'!", ex)
                    }
                }
                LOGGER.info('Compilation successful!')
                LOGGER.info('-'*80)
            }
        }
    }

    private static String padLeft(int len, int number) {
        ' '*(len - String.valueOf(number).length()) + number
    }

    void reloadScript(String scriptId) throws NyException {
        cache.remove(scriptId)
        def reloaded = mapper.reload(scriptId)
        synchronized (clzLoaderLock) {
            try {
                LOGGER.debug('-'*80)
                LOGGER.debug(' Recompiling script: ' + scriptId + '...')
                gcl.parseClass(reloaded.codeSource, true, true)
                LOGGER.debug(' Successfully recompiled the script ' + scriptId)
            } catch (CompilationFailedException ex) {
                LOGGER.error("Compilation error in script '$scriptId'", ex)
                throw new NyException("Compilation error in script '$scriptId'!", ex)
            }
        }
    }

    boolean hasGeneratedQuery(String scriptId) {
        cache.containsKey(scriptId)
    }

    QScript getGeneratedQuery(String scriptId, QSession session) {
        QScript qScript = cache.get(scriptId)
        if (qScript != null) {
            return qScript.spawn(session)
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
        if (configurations.cacheRawScripts()) {
            if (compilerConfigurations.recompileGroovySource) {
                synchronized (clzLoaderLock) {
                    parseAndGet(sourceScript, session, binding)
                }
            } else {
                parseAndGet(sourceScript, session, binding)
            }
        } else {
            GroovyShell shell = new GroovyShell(Thread.currentThread().contextClassLoader, binding, makeCompilerConfigs())
            NyBaseScript parsedScript = sourceScript.parseIn(shell)
            parsedScript.setSession(session)
            parsedScript
        }
    }

    /**
     * Parse the groovy source from class loader and apply the session and bindings.
     *
     * @param sourceScript source script.
     * @param session session instance.
     * @param binding binding instance.
     * @return loaded compiled script.
     */
    private Script parseAndGet(QSource sourceScript, QSession session, Binding binding) {
        Class<?> clazz = gcl.parseClass(sourceScript.codeSource, true)
        NyBaseScript scr = clazz.newInstance() as NyBaseScript
        scr.setBinding(binding)
        scr.setSession(session)
        scr
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

        CompilerConfiguration compilerConfigurations = new CompilerConfiguration()
        compilerConfigurations.scriptBaseClass = NyBaseScript.name

        String[] defImports = configurations.defaultImports()
        if (defImports != null) {
            ImportCustomizer importCustomizer = new ImportCustomizer()
            importCustomizer.addImports(defImports)
            compilerConfigurations.addCompilationCustomizers(importCustomizer)
        }

        // setup recompilation, if specified
        boolean doRecompile = configurations.isAllowRecompilation()
        if (doRecompile) {
            LOGGER.warn('-'*100)
            LOGGER.warn('*** NyQL has enabled to recompile scripts at runtime!')
            LOGGER.warn("*** If this is NOT intentional, then set 'allowRecompilation' flag under 'caching' section to false")
            LOGGER.warn('-'*100)
        }
        compilerConfigurations.setRecompileGroovySource(doRecompile)

        this.compilerConfigurations = compilerConfigurations
        compilerConfigurations
    }

    @Override
    void close() throws IOException {
        if (gcl != null) {
            gcl.close()
        }
    }
}
