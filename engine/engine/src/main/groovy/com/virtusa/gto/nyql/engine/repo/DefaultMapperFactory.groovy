package com.virtusa.gto.nyql.engine.repo

import com.virtusa.gto.nyql.configs.ConfigKeys
import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.QMapperFactory
import com.virtusa.gto.nyql.model.QScriptMapper
import com.virtusa.gto.nyql.utils.QUtils
import groovy.transform.CompileStatic
/**
 * @author iweerarathna
 */
@CompileStatic
class DefaultMapperFactory implements QMapperFactory {

    private static final I_FOLDER = 'folder'
    private static final I_FOLDERS = 'folders'
    private static final I_RESOURCES = 'resources'
    private static final I_RESOURCES_CLZ = QResourceScripts.class.name
    private static final I_FOLDER_CLZ = QScriptsFolder.class.name
    private static final I_FOLDERS_CLZ = QScriptFolders.class.name

    private static final String[] NAMES = [I_FOLDER, I_FOLDERS, I_RESOURCES,
        I_FOLDER_CLZ, I_FOLDERS_CLZ, I_RESOURCES_CLZ]

    @Override
    String[] supportedMappers() {
        NAMES
    }

    @Override
    QScriptMapper create(String implName, Map args, Configurations configurations) {
        if (implName == I_FOLDER || implName == I_FOLDER_CLZ) {
            createNewFolder(args)
        } else if (implName == I_FOLDERS || implName == I_FOLDERS_CLZ) {
            createNewFolders(args)
        } else if (implName == I_RESOURCES || implName == I_RESOURCES_CLZ) {
            createNewResource(args)
        } else {
            throw new NyConfigurationException("Unsupported mapper type '${implName}'! Supports only [${NAMES.join(',')}].")
        }
    }

    private static QResourceScripts createNewResource(Map args) throws NyException {
        if (args == null || args.size() == 0 || !args.resourceRoot) {
            throw new NyConfigurationException("Mandatory parameter 'resourceRoot' is missing for 'resources' mapper!")
        }

        String path = args.resourceRoot ?: ''
        if (path.startsWith('/')) {
            path = path.substring(1);
        }
        new QResourceScripts(path)
    }

    @SuppressWarnings("UnnecessaryGetter")
    private static QScriptFolders createNewFolders(Map args) throws NyException {
        if (args == null || args.size() == 0 || !args.baseDirs) {
            throw new NyConfigurationException("Mandatory parameter 'baseDirs' is missing for 'folders' mapper!")
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
                String configFilePath = args[ConfigKeys.LOCATION_KEY]
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

    @SuppressWarnings('UnnecessaryGetter')
    private static QScriptsFolder createNewFolder(Map args) throws NyException {
        if (args == null || args.size() == 0 || !args.baseDir) {
            throw new NyConfigurationException("Mandatory parameter 'baseDir' is missing for 'folder' mapper!")
        }

        String path = QUtils.readEnv(ConfigKeys.SYS_SCRIPT_DIR, String.valueOf(args.baseDir))
        String inclusionPatterns = args[QScriptsFolder.KEY_INCLUSIONS] ?: ''
        String exclusionPatterns = args[QScriptsFolder.KEY_EXCLUSIONS] ?: ''

        File dir = new File(path)
        if (!dir.exists()) {
            String configFilePath = args[ConfigKeys.LOCATION_KEY]
            if (configFilePath != null) {
                File activeDir = new File(configFilePath).getCanonicalFile()
                if (activeDir.exists() && !dir.isAbsolute()) {
                    File relDir = activeDir.toPath().resolve(path).toFile()
                    return createScriptsFolder(relDir, inclusionPatterns, exclusionPatterns).scanDir()
                }
            }
            throw new NyConfigurationException('Given script folder does not exist! [' + path + ']')
        }

        createScriptsFolder(dir, inclusionPatterns, exclusionPatterns).scanDir()
    }

    private static QScriptsFolder createScriptsFolder(File dir, String inclPatterns, String exclPatterns) {
        QScriptsFolder qScriptsFolder = new QScriptsFolder(dir)
        qScriptsFolder.inclusionPatterns = inclPatterns
        qScriptsFolder.exclusionPatterns = exclPatterns
        qScriptsFolder
    }
}
