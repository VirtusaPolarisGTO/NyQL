package com.virtusa.gto.nyql.engine.repo

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * A class loader specifically being used by NyQL to parse groovy scripts to classes.
 * This was introduced to support recompilation of scripts on demand.
 *
 * @author iweerarathna
 */
@CompileStatic
@PackageScope
class NyGroovyClassLoader extends GroovyClassLoader {

    NyGroovyClassLoader(ClassLoader loader, CompilerConfiguration config) {
        super(loader, config)
    }

    Class parseClass(GroovyCodeSource codeSource, boolean shouldCacheSource, boolean doRecompile) throws CompilationFailedException {
        if (doRecompile) {
            synchronized (sourceCache) {
                sourceCache.remove(codeSource.getName())
            }
        }
        return super.parseClass(codeSource, shouldCacheSource)
    }
}
