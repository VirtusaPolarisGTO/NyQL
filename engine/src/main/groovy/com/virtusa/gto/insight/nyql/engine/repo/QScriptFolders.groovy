package com.virtusa.gto.insight.nyql.engine.repo

import com.virtusa.gto.insight.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.QScriptMapper
import com.virtusa.gto.insight.nyql.model.QSource

/**
 * @author IWEERARATHNA
 */
class QScriptFolders implements QScriptMapper {

    private final List<QScriptsFolder> scriptsFolderList = []
    private final Map<String, QSource> fileMap = [:]

    QScriptFolders(Collection<File> folders) {
        if (folders != null) {
            folders.each { addScriptFolder(it) }
        }
    }

    QScriptFolders addScriptFolder(File file) {
        def impl = new QScriptsFolder(file)
        impl.allSources().each {
            def scriptId = it.id
            if (fileMap.containsKey(scriptsFolderList)) {
                throw new NyConfigurationException("Script by id '$scriptId' already loaded! You are trying to load it " +
                        "from ${file.absolutePath} too!")
            }
            fileMap.put(scriptId, it)
        }
        scriptsFolderList << impl
        return this
    }

    static QScriptFolders createNew(Map args) throws NyException {
        if (args == null || args.size() == 0 || !args.baseDirs) {
            throw new NyConfigurationException('To create a new QScriptsFolder requires at least ' +
                    'one parameter with specifying one or many directories!')
        }

        List<String> paths = args.baseDirs
        List<File> folders = []
        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                String configFilePath = args._location
                if (configFilePath != null) {
                    File activeDir = new File(configFilePath).canonicalFile.parentFile
                    if (activeDir.exists() && !dir.isAbsolute()) {
                        folders << activeDir.toPath().resolve(path).toFile()
                        continue
                    }
                }
                throw new NyConfigurationException("One of script folder does not exist! $path")
            }
        }
        new QScriptFolders(folders)
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
}
