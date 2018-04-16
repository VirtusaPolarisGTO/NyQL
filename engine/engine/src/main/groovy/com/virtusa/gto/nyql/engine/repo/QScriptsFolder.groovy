package com.virtusa.gto.nyql.engine.repo

import com.virtusa.gto.nyql.configs.ConfigKeys
import com.virtusa.gto.nyql.exceptions.NyScriptNotFoundException
import com.virtusa.gto.nyql.model.QFileSource
import com.virtusa.gto.nyql.model.QScriptMapper
import com.virtusa.gto.nyql.model.QSource
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets
import java.nio.file.*
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

    static final String KEY_INCLUSIONS = 'inclusions'
    static final String KEY_EXCLUSIONS = 'exclusions'

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
        LOGGER.info("Loading script files from directory '{}'", baseDir.canonicalPath)

        Path canPath = baseDir.toPath()
        ScriptVisitor visitor = new ScriptVisitor(this, canPath, inclusionPatterns, exclusionPatterns)
        visitor.start()
        prettyPrintFiles()
        this
    }

    private QSource processScript(File file) {
        String relPath = captureFileName(baseDir.toPath().relativize(file.toPath()).toString()).replace('\\', '/')
        def qSrc = createSourceScript(file, baseDir)

        fileMap[relPath] = qSrc
        maxLen = Math.max(relPath.length(), maxLen)
        qSrc
    }

    static QSource createSourceScript(File file, File baseDir) {
        String relPath = captureFileName(baseDir.toPath().relativize(file.toPath()).toString()).replace('\\', '/')

        String content = readAll(file)
        GroovyCodeSource groovyCodeSource = new GroovyCodeSource(content, relPath, GroovyShell.DEFAULT_CODE_BASE)
        groovyCodeSource.setCachable(true)

        new QFileSource(relPath, file, groovyCodeSource)
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

    @Override
    QSource map(String id) throws NyScriptNotFoundException {
        QSource source = fileMap[id]
        if (source == null) {
            File scriptFresh = baseDir.toPath().resolve(id + ConfigKeys.GROOVY_EXT).toFile()
            if (scriptFresh.exists()) {
                LOGGER.debug('Loading a fresh script from ' + id + '...')
                source = processScript(scriptFresh)
            } else {
                scriptFresh = baseDir.toPath().resolve(id + ConfigKeys.NYQL_EXT).toFile();
                if (scriptFresh.exists()) {
                    LOGGER.debug('Loading a fresh script from ' + id + '...')
                    source = processScript(scriptFresh)
                } else {
                    throw new NyScriptNotFoundException(id)
                }
            }
        }
        if (!source.isValid()) {
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

    @Override
    QSource reload(String id) throws NyScriptNotFoundException {
        fileMap.remove(id)
        QSource srcNew = map(id)
        fileMap[id] = srcNew
        srcNew
    }

    private void prettyPrintFiles() {
        fileMap.keySet().sort().each {
            String kb = toKB((fileMap[it] as QFileSource).file.length())
            LOGGER.info(' > ' + it.padRight(maxLen + 5) + "${kb.padLeft(6)}")
        }
        LOGGER.info("Found ${fileMap.size()} script(s).")
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

        @SuppressWarnings('UnnecessaryGetter')
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

        private static boolean isEndsWithAny(String text, String... checks) {
            for (String s : checks) {
                if (text.endsWith(s)) {
                    return true
                }
            }
            false
        }

        @Override
        FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!attrs.directory &&
                    !isEndsWithAny(file.getFileName().toString().toLowerCase(), ConfigKeys.GROOVY_EXT, ConfigKeys.NYQL_EXT)) {
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
