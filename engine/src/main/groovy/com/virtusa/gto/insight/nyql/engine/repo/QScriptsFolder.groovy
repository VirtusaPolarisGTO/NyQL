package com.virtusa.gto.insight.nyql.engine.repo

import com.virtusa.gto.insight.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.QScriptMapper
import com.virtusa.gto.insight.nyql.model.QSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Contains script mapping from a single folder in the system.
 *
 * @author IWEERARATHNA
 */
class QScriptsFolder implements QScriptMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(QScriptsFolder.class)

    private static final boolean DEF_CACHING = false

    final File baseDir
    String inclusionPatterns
    String exclusionPatterns
    private int maxLen = 1

    private final Map<String, QSource> fileMap = [:]

    QScriptsFolder(File theBaseDir) {
        baseDir = theBaseDir.getCanonicalFile()
    }

    QScriptsFolder scanDir() {
        // scans the given directory
        LOGGER.debug("Loading script files from directory '{}'", baseDir.canonicalPath)

        Path canPath = baseDir.toPath()
        ScriptVisitor visitor = new ScriptVisitor(this, canPath, inclusionPatterns, exclusionPatterns)
        visitor.start()
        prettyPrintFiles()
        return this
    }

    private void processScript(File file) {
        String relPath = captureFileName(baseDir.toPath().relativize(file.toPath()).toString()).replace('\\', '/')

        String content = readAll(file)
        GroovyCodeSource groovyCodeSource = new GroovyCodeSource(content, relPath, GroovyShell.DEFAULT_CODE_BASE)
        groovyCodeSource.setCachable(true)

        def qSrc = new QSource(id: relPath, file: file, doCache: DEF_CACHING, codeSource: groovyCodeSource)
        fileMap[relPath] = qSrc
        maxLen = Math.max(relPath.length(), maxLen)
    }

    private static String readAll(File file) {
        return file.getText(StandardCharsets.UTF_8.name());
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
            throw new NyConfigurationException('To create a new QScriptsFolder requires at least one parameter with specifying base directory!')
        }

        String path = args.baseDir;
        File dir = new File(path);
        if (!dir.exists()) {
            String configFilePath = args._location;
            if (configFilePath != null) {
                File activeDir = new File(configFilePath).getCanonicalFile().getParentFile()
                if (activeDir.exists() && !dir.isAbsolute()) {
                    QScriptsFolder qScriptsFolder = new QScriptsFolder(activeDir.toPath().resolve(path).toFile())
                    qScriptsFolder.inclusionPatterns = args['inclusions'] ?: ''
                    qScriptsFolder.exclusionPatterns = args['exclusions'] ?: ''
                    return qScriptsFolder.scanDir()
                }
            }
            throw new NyConfigurationException('Given script folder does not exist! [' + args[0] + ']')
        }
        QScriptsFolder qScriptsFolder = new QScriptsFolder(dir);
        qScriptsFolder.inclusionPatterns = args['inclusions'] ?: ''
        qScriptsFolder.exclusionPatterns = args['exclusions'] ?: ''
        return qScriptsFolder.scanDir()
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

    private void prettyPrintFiles() {
        fileMap.keySet().sort().each {
            String kb = toKB(fileMap[it].file.length())
            LOGGER.debug(' > ' + it.padRight(maxLen + 5) + "${kb.padLeft(6)}")
        }
        LOGGER.debug("Found ${fileMap.size()} script(s).")
    }

    private static String toKB(long length) {
        return ((int)Math.ceil(length / 1024.0)) + ' KB'
    }

    private static class ScriptVisitor extends SimpleFileVisitor<Path> {

        private final List<PathMatcher> inclusions = new LinkedList<>()
        private final List<PathMatcher> exclusions = new LinkedList<>()
        private final QScriptsFolder scriptsFolder
        private final Path startDir

        ScriptVisitor(QScriptsFolder qScriptsFolder, Path rootDir, String patternInclusions, String patternExclusions) {
            scriptsFolder = qScriptsFolder
            startDir = rootDir
            if (patternInclusions.trim().length() > 0) {
                inclusions.addAll(Stream.of(patternInclusions.split('[,]'))
                        .map({ FileSystems.getDefault().getPathMatcher('glob:' + it) })
                        .collect(Collectors.toList()))
            }
            if (patternExclusions.trim().length() > 0) {
                exclusions.addAll(Stream.of(patternExclusions.split('[,]'))
                        .map({ FileSystems.getDefault().getPathMatcher('glob:' + it) })
                        .collect(Collectors.toList()))
            }
        }

        @Override
        FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!attrs.directory && !file.getFileName().toString().toLowerCase().endsWith(".groovy")) {
                return FileVisitResult.SKIP_SUBTREE
            }

            if (check(file)) {
                if (!attrs.directory) {
                    scriptsFolder.processScript(file.toFile())
                }
                return FileVisitResult.CONTINUE
            } else {
                return FileVisitResult.SKIP_SUBTREE
            }
        }

        private boolean check(Path file) {
            def relPath = startDir.relativize(file)
            if (inclusions.any { it.matches(relPath) }) {
                return true
            }
            if (exclusions.any { it.matches(relPath) }) {
                return false
            }

            return inclusions.size() <= 0
        }

        void start() {
            Files.walkFileTree(startDir, this)
        }
    }
}
