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

    @SuppressWarnings("UnnecessaryGetter")
    static QScriptFolders createNew(Map args) throws NyException {
        if (args == null || args.size() == 0 || !args.baseDirs) {
            throw new NyConfigurationException('To create a new QScriptsFolder requires at least ' +
                    'one parameter with specifying one or many directories!')
        }

        List<Object> paths = (List<Object>) args.baseDirs
        List<List<?>> folders = []
        for (Object item : paths) {
            List<?> list
            if (item instanceof String) {
                list = [(String)item, null, null]
            } else if (item instanceof Map) {
                Map tmp = (Map)item
                list = [(String)tmp['baseDir'], (String)tmp[QScriptsFolder.KEY_INCLUSIONS], (String)tmp[QScriptsFolder.KEY_EXCLUSIONS]]
            } else {
                throw new NyConfigurationException('Unknown argument type received specifying multiple script directories!')
            }

            String incl = list.get(1) ?: ''
            String excl = list.get(2) ?: ''

            String path = (String)list.get(0)
            File dir = new File(path)
            if (!dir.exists()) {
                String configFilePath = args._location
                if (configFilePath != null) {
                    File activeDir = new File(configFilePath).getCanonicalFile().getParentFile()
                    if (activeDir.exists() && !dir.isAbsolute()) {
                        folders << [activeDir.toPath().resolve(path).toFile(), incl, excl]
                        continue
                    }
                }
                throw new NyConfigurationException("One of script folder does not exist! $path")
            } else {
                folders << [dir, incl, excl]
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
