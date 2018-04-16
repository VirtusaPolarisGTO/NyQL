package com.virtusa.gto.nyql.engine.repo

import com.virtusa.gto.nyql.configs.ConfigKeys
import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.exceptions.NyScriptNotFoundException
import com.virtusa.gto.nyql.model.QScriptMapper
import com.virtusa.gto.nyql.model.QSource
import groovy.transform.CompileStatic
/**
 * Stores script mapping from several folders in the system.
 *
 * @author IWEERARATHNA
 */
@CompileStatic
class QScriptFolders implements QScriptMapper {

    private final List<QScriptsFolder> scriptsFolderList = []
    private final Map<String, QSource> fileMap = [:]
    private final Map<String, QScriptsFolder> scriptsFolderMap = [:]

    QScriptFolders(Collection<List<?>> folders) {
        if (folders != null) {
            folders.each { addScriptFolder(it) }
        }
    }

    QScriptFolders addScriptFolder(List<?> props) {
        File file = (File) props.get(0)
        QScriptsFolder qScriptsFolder = new QScriptsFolder(file)
        if (props.size() > 1 && props.get(1) != null) {
             qScriptsFolder.inclusionPatterns = (String)props.get(1)
        }
        if (props.size() > 2 && props.get(2) != null) {
            qScriptsFolder.exclusionPatterns = (String)props.get(2)
        }

        def impl = qScriptsFolder.scanDir()
        impl.allSources().each {
            def scriptId = it.id
            if (fileMap.containsKey(scriptsFolderList)) {
                throw new NyConfigurationException("Script by id '$scriptId' already loaded! You are trying to load it " +
                        "from ${file.absolutePath} too!")
            }
            fileMap.put(scriptId, it)
            scriptsFolderMap.put(scriptId, impl)
        }
        scriptsFolderList << impl
        this
    }

    @Override
    QSource map(String id) {
        fileMap[id]
    }

    @Override
    Collection<QSource> allSources() {
        fileMap.values()
    }

    @Override
    boolean canCacheAtStartup() {
        true
    }

    @Override
    QSource reload(String id) throws NyScriptNotFoundException {
        def folderOfFile = scriptsFolderMap[id] as QScriptsFolder
        if (folderOfFile == null) {
            throw new NyScriptNotFoundException('There is no record of from which directory the script ' + id + ' has been loaded!')
        }

        File baseDir = folderOfFile.baseDir
        File sFile = new File(baseDir, id + ConfigKeys.GROOVY_EXT)
        if (!sFile.exists()) {
            sFile = new File(baseDir, id + ConfigKeys.NYQL_EXT)
        }
        def script = QScriptsFolder.createSourceScript(sFile, baseDir)

        fileMap.put(id, script)
        map(id)
    }
}
