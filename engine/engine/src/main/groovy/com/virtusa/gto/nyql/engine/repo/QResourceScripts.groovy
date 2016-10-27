package com.virtusa.gto.nyql.engine.repo

import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.exceptions.NyScriptNotFoundException
import com.virtusa.gto.nyql.model.QScriptMapper
import com.virtusa.gto.nyql.model.QSource

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
        if (args == null || args.size() == 0 || !args.resourceDir) {
            throw new NyConfigurationException('To create a new QResourceScripts requires at least one parameter with specifying a resource directory!')
        }

        String path = args.resourceDir ?: '/'
        new QResourceScripts(path)
    }

    @Override
    QSource map(String id) throws NyScriptNotFoundException {
        if (resMap.containsKey(id)) {
            resMap[id]
        } else {
            String content = readAll(rootRes + id)
            GroovyCodeSource groovyCodeSource = new GroovyCodeSource(content, id, GroovyShell.DEFAULT_CODE_BASE)
            groovyCodeSource.setCachable(true)

            def qSrc = new QSource(id: id, file: null, codeSource: groovyCodeSource)
            resMap[id] = qSrc
            qSrc
        }
    }

    private static String readAll(String subPath) throws NyScriptNotFoundException {
        def stream = Thread.currentThread().contextClassLoader.getResourceAsStream(subPath)
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

    @Override
    Collection<QSource> allSources() {
        throw new NyException('There is no way to retrieve all sources from java resource directory ' +
                'WITHOUT using a third party library!')
    }

    @Override
    boolean canCacheAtStartup() {
        false
    }
}
