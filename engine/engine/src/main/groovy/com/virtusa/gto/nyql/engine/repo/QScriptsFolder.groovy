package com.virtusa.gto.nyql.engine.repo

import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.exceptions.NyScriptNotFoundException
import com.virtusa.gto.nyql.model.QFileSource
import com.virtusa.gto.nyql.model.QScriptMapper
import com.virtusa.gto.nyql.model.QSource
import groovy.transform.CompileStatic
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
@CompileStatic
class QScriptsFolder implements QScriptMapper {

    private static final String KEY_INCLUSIONS = 'inclusions'
    private static final String KEY_EXCLUSIONS = 'exclusions'
    private static final String GLOB_NAME = 'glob:'
    private static final Logger LOGGER = LoggerFactory.getLogger(QScriptsFolder)

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
        this
    }

    private void processScript(File file) {
        String relPath = captureFileName(baseDir.toPath().relativize(file.toPath()).toString()).replace('\\', '/')

        String content = readAll(file)
        GroovyCodeSource groovyCodeSource = new GroovyCodeSource(content, relPath, GroovyShell.DEFAULT_CODE_BASE)
        groovyCodeSource.setCachable(true)

        def qSrc = new QFileSource(relPath, file, groovyCodeSource)
        fileMap[relPath] = qSrc
        maxLen = Math.max(relPath.length(), maxLen)
    }

    private static String readAll(File file) {
        file.getText(StandardCharsets.UTF_8.name())
    }

    private static String captureFileName(String path) {
        int lp = path.lastIndexOf('.')
        if (lp > 0) {
            return path.substring(0, lp)
        }
        path
    }

    @SuppressWarnings("UnnecessaryGetter")
    static QScriptsFolder createNew(Map args) throws NyException {
        if (args == null || args.size() == 0 || !args.baseDir) {
            throw new NyConfigurationException('To create a new QScriptsFolder requires at least one parameter with specifying base directory!')
        }

        String path = args.baseDir
        File dir = new File(path)
        if (!dir.exists()) {
            String configFilePath = args._location
            if (configFilePath != null) {
                File activeDir = new File(configFilePath).getCanonicalFile()
                if (activeDir.exists() && !dir.isAbsolute()) {
                    QScriptsFolder qScriptsFolder = new QScriptsFolder(activeDir.toPath().resolve(path).toFile())
                    qScriptsFolder.inclusionPatterns = args[KEY_INCLUSIONS] ?: ''
                    qScriptsFolder.exclusionPatterns = args[KEY_EXCLUSIONS] ?: ''
                    return qScriptsFolder.scanDir()
                }
            }
            throw new NyConfigurationException('Given script folder does not exist! [' + path + ']')
        }
        QScriptsFolder qScriptsFolder = new QScriptsFolder(dir)
        qScriptsFolder.inclusionPatterns = args[KEY_INCLUSIONS] ?: ''
        qScriptsFolder.exclusionPatterns = args[KEY_EXCLUSIONS] ?: ''
        qScriptsFolder.scanDir()
    }

    @Override
    QSource map(String id) throws NyScriptNotFoundException {
        QSource source = fileMap[id]
        if (source == null || !source.isValid()) {
            throw new NyScriptNotFoundException(id)
        }
        source
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
            String kb = toKB((fileMap[it] as QFileSource).file.length())
            LOGGER.debug(' > ' + it.padRight(maxLen + 5) + "${kb.padLeft(6)}")
        }
        LOGGER.debug("Found ${fileMap.size()} script(s).")
    }

    private static String toKB(long length) {
        ((int)Math.ceil(length / 1024.0)) + ' KB'
    }

    @CompileStatic
    private static class ScriptVisitor extends SimpleFileVisitor<Path> {

        private static final String SPLIT_PATTERN = '[,]'
        private final List<PathMatcher> inclusions = new LinkedList<>()
        private final List<PathMatcher> exclusions = new LinkedList<>()
        private final QScriptsFolder scriptsFolder
        private final Path startDir

        @SuppressWarnings("UnnecessaryGetter")
        ScriptVisitor(QScriptsFolder qScriptsFolder, Path rootDir, String patternInclusions, String patternExclusions) {
            scriptsFolder = qScriptsFolder
            startDir = rootDir
            if (patternInclusions.trim().length() > 0) {
                inclusions.addAll(Stream.of(patternInclusions.split(SPLIT_PATTERN))
                        .map { FileSystems.getDefault().getPathMatcher(GLOB_NAME + it) }
                        .collect(Collectors.toList()))
            }
            if (patternExclusions.trim().length() > 0) {
                exclusions.addAll(Stream.of(patternExclusions.split(SPLIT_PATTERN))
                        .map { FileSystems.getDefault().getPathMatcher(GLOB_NAME + it) }
                        .collect(Collectors.toList()))
            }
        }

        @Override
        FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!attrs.directory && !file.getFileName().toString().toLowerCase().endsWith('.groovy')) {
                return FileVisitResult.SKIP_SUBTREE
            }

            if (check(file)) {
                if (!attrs.directory) {
                    scriptsFolder.processScript(file.toFile())
                }
                return FileVisitResult.CONTINUE
            }
            FileVisitResult.SKIP_SUBTREE
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
