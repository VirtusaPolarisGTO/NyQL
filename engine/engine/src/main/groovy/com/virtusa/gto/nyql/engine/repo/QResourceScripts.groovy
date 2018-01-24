package com.virtusa.gto.nyql.engine.repo

import com.virtusa.gto.nyql.configs.ConfigKeys
import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.exceptions.NyScriptNotFoundException
import com.virtusa.gto.nyql.model.QScriptMapper
import com.virtusa.gto.nyql.model.QSource
import com.virtusa.gto.nyql.model.QUriSource
import groovy.transform.CompileStatic

import java.nio.charset.StandardCharsets
/**
 * Loads scripts from a resource folder.
 *
 * @author IWEERARATHNA
 */
class QResourceScripts implements QScriptMapper {

    private final String rootRes

    private final Map<String, QSource> resMap = [:]

    QResourceScripts(String theRootDir) {
        rootRes = theRootDir
    }

    static QResourceScripts createNew(Map args) throws NyException {
        if (args == null || args.size() == 0 || !args.resourceRoot) {
            throw new NyConfigurationException('To create a new QResourceScripts requires at least one parameter with specifying a resource directory!')
        }

        // FIX for issue #52
        String path = args.resourceRoot ?: ''
        if (path.startsWith('/')) {
            path = path.substring(1);
        }
        new QResourceScripts(path)
    }

    @Override
    QSource map(String id) throws NyScriptNotFoundException {
        if (resMap.containsKey(id)) {
            resMap[id]
        } else {
            String content = readAll(rootRes + id + ConfigKeys.GROOVY_EXT)
            GroovyCodeSource groovyCodeSource = new GroovyCodeSource(content, id, GroovyShell.DEFAULT_CODE_BASE)
            groovyCodeSource.setCachable(true)

            def qSrc = new QUriSource(id, null, groovyCodeSource)
            resMap[id] = qSrc
            qSrc
        }
    }

    @CompileStatic
    private static String readAll(String subPath) throws NyScriptNotFoundException {
        URL url = Thread.currentThread().contextClassLoader.getResource(subPath)
        InputStream stream = openSafe(url)
        if (stream != null) {
            try {
                return stream.readLines(StandardCharsets.UTF_8.name()).join('\n')
            } finally {
                if (stream != null) {
                    stream.close()
                }
            }
        }
        throw new NyScriptNotFoundException("There is no resource exist in $subPath!")
    }

    @CompileStatic
    private static InputStream openSafe(URL url) {
        try {
            url != null ? url.openStream() : null
        } catch (IOException ignored) {
            null
        }
    }

    @Override
    Collection<QSource> allSources() {
        throw new NyException('There is no way to retrieve all sources from java resource directory ' +
                'WITHOUT using a third party library!')
    }

    @Override
    boolean canCacheAtStartup() {
        false
    }

    @Override
    QSource reload(String id) throws NyScriptNotFoundException {
        map(id)
    }
}
