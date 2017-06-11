package com.virtusa.gto.nyql.engine.repo

import com.virtusa.gto.nyql.configs.ConfigKeys
import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.exceptions.NyException
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

    QScriptFolders(Collection<File> folders) {
        if (folders != null) {
            folders.each { addScriptFolder(it) }
        }
    }

    QScriptFolders addScriptFolder(File file) {
        def impl = new QScriptsFolder(file).scanDir()
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

    @SuppressWarnings("UnnecessaryGetter")
    static QScriptFolders createNew(Map args) throws NyException {
        if (args == null || args.size() == 0 || !args.baseDirs) {
            throw new NyConfigurationException('To create a new QScriptsFolder requires at least ' +
                    'one parameter with specifying one or many directories!')
        }

        List<String> paths = (List<String>) args.baseDirs
        List<File> folders = []
        for (String path : paths) {
            File dir = new File(path)
            if (!dir.exists()) {
                String configFilePath = args._location
                if (configFilePath != null) {
                    File activeDir = new File(configFilePath).getCanonicalFile().getParentFile()
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

    @Override
    QSource reload(String id) throws NyScriptNotFoundException {
        def folderOfFile = scriptsFolderMap[id] as QScriptsFolder
        if (folderOfFile == null) {
            throw new NyScriptNotFoundException('There is no record of from which directory the script ' + id + ' has been loaded!')
        }

        File baseDir = folderOfFile.baseDir
        File sFile = new File(baseDir, id + ConfigKeys.GROOVY_EXT)
        def script = QScriptsFolder.createSourceScript(sFile, baseDir)

        fileMap.put(id, script)
        map(id)
    }
}
