package com.virtusa.gto.insight.nyql.engine.repo

import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.QScriptMapper
import com.virtusa.gto.insight.nyql.model.QSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author IWEERARATHNA
 */
class QScriptsFolder implements QScriptMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(QScriptsFolder.class)

    private static final boolean DEF_CACHING = false

    final File baseDir

    private final Map<String, QSource> fileMap = [:]

    QScriptsFolder(File theBaseDir) {
        baseDir = theBaseDir

        // scans the given directory
        LOGGER.debug("Loading script files from directory '{}'", baseDir.absolutePath)
        scanDir(baseDir)
    }

    private void scanDir(File dir) {
        if (dir.isDirectory()) {
            def files = dir.listFiles(new FilenameFilter() {
                @Override
                boolean accept(File d, String name) {
                    return d.isDirectory() || name.endsWith(".groovy")
                }
            })

            files.each {
                if (it.isDirectory()) {
                    scanDir(it)
                } else {
                    String relPath = captureFileName(baseDir.toPath().relativize(it.toPath()).toString()).replace('\\', '/')
                    def qSrc = new QSource(id: relPath, file: it, doCache: DEF_CACHING)
                    fileMap[relPath] = qSrc
                    LOGGER.debug("  > {}", relPath)
                }
            }
        }

    }

    private static String captureFileName(String path) {
        int lp = path.lastIndexOf('.')
        if (lp > 0) {
            return path.substring(0, lp)
        }
        return path
    }

    static QScriptsFolder createNew(Map args) throws NyException {
        if (args == null || args.size() == 0 || !args.baseDir) {
            throw new NyException("To create a new QScriptsFolder requires at least one parameter with specifying base directory!")
        }

        String path = args.baseDir;
        File dir = new File(path);
        if (!dir.exists()) {
            String configFilePath = args._location;
            if (configFilePath != null) {
                File activeDir = new File(configFilePath).canonicalFile.getParentFile()
                if (activeDir.exists() && !dir.isAbsolute()) {
                    return new QScriptsFolder(activeDir.toPath().resolve(path).toFile())
                }
            }
            throw new NyException("Given script folder does not exist! [" + args[0] + "]")
        }
        return new QScriptsFolder(dir);
    }

    @Override
    QSource map(String id) {
        return fileMap[id]
    }

    @Override
    Collection<QSource> allSources() {
        return fileMap.values()
    }
}
