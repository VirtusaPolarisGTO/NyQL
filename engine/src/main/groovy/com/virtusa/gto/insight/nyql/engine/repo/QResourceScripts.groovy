package com.virtusa.gto.insight.nyql.engine.repo

import com.virtusa.gto.insight.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.QScriptMapper
import com.virtusa.gto.insight.nyql.model.QSource

import java.nio.charset.StandardCharsets

/**
 * Loads scripts from a resource folder.
 *
 * @author IWEERARATHNA
 */
class QResourceScripts implements QScriptMapper {

    private final String rootRes

    private final Map<String, QSource> resMap = [:]

    public QResourceScripts(String theRootDir) {
        rootRes = theRootDir
    }

    static QResourceScripts createNew(Map args) throws NyException {
        if (args == null || args.size() == 0 || !args.resourceDir) {
            throw new NyConfigurationException("To create a new QResourceScripts requires at least one parameter with specifying a resource directory!")
        }

        String path = args.resourceDir ?: "/";
        return new QResourceScripts(path);
    }

    @Override
    QSource map(String id) {
        if (resMap.containsKey(id)) {
            return resMap[id]
        } else {
            String content = readAll(rootRes + id)
            GroovyCodeSource groovyCodeSource = new GroovyCodeSource(content, id, GroovyShell.DEFAULT_CODE_BASE)
            groovyCodeSource.setCachable(true)

            def qSrc = new QSource(id: id, file: null, doCache: false, codeSource: groovyCodeSource)
            resMap[id] = qSrc
            return qSrc
        }
    }

    private static String readAll(String subPath) {
        def stream = Thread.currentThread().contextClassLoader.getResourceAsStream(subPath)
        if (stream != null) {
            try {
                return stream.readLines(StandardCharsets.UTF_8.name()).join("\n")
            } finally {
                if (stream != null) {
                    stream.close()
                }
            }
        }
        throw new NyConfigurationException("There is no resource exist in '$subPath'!")
    }

    @Override
    Collection<QSource> allSources() {
        throw new NyException("You can't retrieve all sources from java resource directory!")
    }

    @Override
    boolean canCacheAtStartup() {
        return false
    }
}
