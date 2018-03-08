package com.virtusa.gto.nyql.engine
@java.lang.SuppressWarnings('JdbcConnectionReference')
import com.virtusa.gto.nyql.configs.ConfigBuilder
import com.virtusa.gto.nyql.configs.ConfigKeys
import com.virtusa.gto.nyql.configs.ConfigParser
import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.utils.QUtils
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets
/**
 * Main interface to interact with NyQL queries.
 *
 * @author IWEERARATHNA
 *
 */
@SuppressWarnings('CatchException')
@Deprecated
@PackageScope
class NyQL {

    private static final Logger LOGGER = LoggerFactory.getLogger(NyQL)

    private static final String BOOTSTRAP_KEY = 'com.virtusa.gto.nyql.autoBootstrap'
    private static final String AUTO_SHUTDOWN_KEY = 'com.virtusa.gto.nyql.addShutdownHook'
    private static final String LOAD_CLASSPATH_KEY = 'com.virtusa.gto.nyql.classpathBootstrap'
    private static final String CONFIG_PATH_KEY = 'com.virtusa.gto.nyql.nyConfigFile'
    private static final String MODE_KEY = 'com.virtusa.gto.nyql.mode'

    private static final int STAR_LEN = 100
    private static final String TRUE_STR = 'true'
    private static final String FALSE_STR = 'false'
    private static final String JSON_CONFIG_FILENAME = 'nyql.json'
    private static final String NY_UNKNOWN_VERSION = '[UNKNOWN]'

    private static NyQLInstance nyQLInstance

    static {
        boolean serverMode = QUtils.readEnv(MODE_KEY, "").equalsIgnoreCase("server")

        try {
            if (!serverMode && !Boolean.parseBoolean(QUtils.readEnv(BOOTSTRAP_KEY, TRUE_STR))) {
                LOGGER.warn('*' * STAR_LEN)
                LOGGER.warn('You MUST EXPLICITLY setup NyQL with programmatically or configuration json file!')
                LOGGER.warn('*' * STAR_LEN)
                return
            }

            LOGGER.debug(' >>> Running NyQL version: ' + readVersion() + ' <<<')
            LOGGER.debug(' ' * STAR_LEN)

            configure()

            if (Boolean.parseBoolean(QUtils.readEnv(AUTO_SHUTDOWN_KEY, FALSE_STR))) {
                LOGGER.warn('Automatically adding a NyQL shutdown hook...')
                Runtime.runtime.addShutdownHook(new Thread ({ shutdown() }))
            } else if (!serverMode) {
                LOGGER.warn('*' * STAR_LEN)
                LOGGER.warn('You MUST EXPLICITLY Call SHUTDOWN method of NyQL when you are done with this!')
                LOGGER.warn('*' * STAR_LEN)
            }
        } catch (Exception ex) {
            LOGGER.error('Error occurred while initializing NyQL!', ex)
            throw ex
        }
    }

    @CompileStatic
    private static String readVersion() {
        InputStream inputStream
        try {
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("nyql_buildinfo.properties")
            Properties properties = new Properties()
            properties.load(inputStream)
            return properties.getProperty("nyql.version", NY_UNKNOWN_VERSION)

        } catch (IOException ex) {
            LOGGER.error('Unable to determine NyQL version!', ex)
            return NY_UNKNOWN_VERSION
        } finally {
            if (inputStream != null) {
                inputStream.close()
            }
        }
    }

    /**
     * Configure NyQL with a configuration json file. If the input json file is <code>null</code>,
     * then it will look for a file in classpath/working directory.
     *
     * @param inputJson input json file.
     * @param force if specified true, then configurations will be reloaded.
     */
    static void configure(File inputJson=null, boolean force=false) {
        boolean serverMode = QUtils.readEnv(MODE_KEY, "").equalsIgnoreCase("server")
        if (!Configurations.instance().isConfigured() || force) {
            LOGGER.warn('NyQL is going to configure with default configurations using classpath...')
            if (!configFromSystemProperty() && !configFromClasspath()) {
                File nyConfig = inputJson ?: new File(JSON_CONFIG_FILENAME)
                if (!nyConfig.exists()) {
                    if (!serverMode) {
                        LOGGER.error('*' * STAR_LEN)
                        LOGGER.error("No nyql.json file is found on classpath! [${nyConfig.absolutePath}]")
                        LOGGER.error(' ' * (STAR_LEN / 2))
                        LOGGER.error('Explicitly call the configure method with configuration input file!')
                        LOGGER.error('*' * STAR_LEN)
                    }
                    //throw new RuntimeException("No '$JSON_CONFIG_FILENAME' file is found on classpath! [" + nyConfig.absolutePath + "]");
                } else {
                    LOGGER.debug("Loading configurations from ${nyConfig.canonicalPath}...")
                    Map configData = ConfigParser.parseAndResolve(nyConfig)
                    configData.put(ConfigKeys.LOCATION_KEY, new File('.').canonicalPath)
                    Configurations configurations = ConfigBuilder.instance().setupFrom(configData).build()
                    nyQLInstance = NyQLInstance.create(configurations)
                }
            }

        } else {
            LOGGER.warn('NyQL has already been configured!')
        }
    }

    /**
     * Config NyQL from the file specified in system property.
     *
     * @return true if configured from system property.
     */
    private static boolean configFromSystemProperty() {
        String path = QUtils.readEnv(CONFIG_PATH_KEY, null)
        if (path != null) {
            LOGGER.debug('NyQL is configuring from path specified in system property: ' + path)
            File inputConfig = new File(path)
            if (inputConfig.exists()) {
                Map configData = ConfigParser.parseAndResolve(inputConfig)
                configData.put(ConfigKeys.LOCATION_KEY, inputConfig.canonicalPath)
                Configurations configInst = ConfigBuilder.instance().setupFrom(configData).build()
                nyQLInstance = NyQLInstance.create(configInst)
                return true
            } else {
                throw new IOException('Given configuration file does not exist! ' + path)
            }
        }
        false
    }

    /**
     * Config NyQL from classpath.
     *
     * @return true if successfully configured from
     */
    private static boolean configFromClasspath() {
        if (!asBoolean(QUtils.readEnv(LOAD_CLASSPATH_KEY, TRUE_STR))) {
            LOGGER.warn('NyQL configuration from classpath has been disabled!')
            return false
        }

        def res = Thread.currentThread().contextClassLoader.getResourceAsStream(JSON_CONFIG_FILENAME)
        if (res != null) {
            try {
                LOGGER.debug('Loading configurations from classpath...')
                Map configData = new JsonSlurper().parse(res, StandardCharsets.UTF_8.name()) as Map
                Configurations configInst = ConfigBuilder.instance().setupFrom(configData).build()
                nyQLInstance = NyQLInstance.create(configInst)
                return true
            } finally {
                res.close()
            }
        }
        false
    }

    /**
     * <p>
     * Parse the given file indicated from given script name and returns the generated query
     * with its other information. Your script name would be the relative path from the
     * script root directory, always having forward slashes (/).
     * </p>
     * You should call this <b>only</b> if you are working with a script repository.
     *
     * @param scriptName name of the script.
     * @return generated query instance.
     * @throws NyException any exception thrown while parsing.
     */
    @CompileStatic
    static QScript parse(String scriptName) throws NyException {
        nyQLInstance.parse(scriptName)
    }

    /**
     * <p>
     * Parse the given file indicated from given script name using the given variable set
     * and returns the generated query
     * with its other information. Your script name would be the relative path from the
     * script root directory, always having forward slashes (/).
     * </p>
     * You should call this <b>only</b> if you are working with a script repository.
     *
     * @param scriptName name of the script.
     * @param data set of variable data required for generation of query.
     * @return generated query instance.
     * @throws NyException any exception thrown while parsing.
     */
    @CompileStatic
    static QScript parse(String scriptName, Map<String, Object> data) throws NyException {
        nyQLInstance.parse(scriptName, data)
    }

    /**
     * Shutdown the nyql engine.
     * This should be called only when your application exits.
     */
    @CompileStatic
    static void shutdown() {
        nyQLInstance.shutdown()
    }

    /**
     * Allows recompiling a script when it is already compiled and cached. You may call
     * this method at runtime, but it does not reload or recompile unless scripts are loaded from
     * file.
     *
     * @param scriptName unique script name.
     * @throws NyException any exception thrown while recompiling.
     * @since v2
     */
    @CompileStatic
    static void recompileScript(String scriptName) throws NyException {
        nyQLInstance.recompileScript(scriptName)
    }

    static Configurations getConfigurations() {
        nyQLInstance.configurations
    }

    /**
     * <p>
     * Executes a given file indicated by the script name and returns the final result
     * which was output of the last statement in the script ran.
     * </p>
     * <p>
     * This method will automatically parse the script and execute using internally
     * configured executor.
     * </p>
     *
     * @param scriptName name of the script to be run.
     * @return the result of the script execution.
     * @throws NyException any exception thrown while parsing or executing.
     */
    @CompileStatic
    static <T> T execute(String scriptName) throws NyException {
        nyQLInstance.execute(scriptName)
    }

    /**
     * <p>
     * Executes a given file indicated by the script name using given set of variables
     * and returns the final result
     * which was output of the last statement in the script ran.
     * </p>
     * <p>
     * This method will automatically parse the script and execute using internally
     * configured executor.
     * </p>
     * <b>Note:</b><br/>
     * You should pass all parameter values required for the query execution.
     *
     * @param scriptName name of the script to be run.
     * @param data set of variables to be passed to the script run.
     * @return the result of the script execution.
     * @throws NyException any exception thrown while parsing or executing.
     */
    @CompileStatic
    static <T> T execute(String scriptName, Map<String, Object> data) throws NyException {
        nyQLInstance.execute(scriptName, data)
    }

    /**
     * Executes the given script and returns the result as a json string.
     * <p>
     *     If you still want to parse the json again, use the other execute method
     *     <code>execute(String, Map)</code>.
     * </p>
     *
     * @param scriptName name of the script to run.
     * @param data set of variables required for script.
     * @return result as json string.
     * @throws NyException any exception thrown while executing and parsing.
     */
    @CompileStatic
    static String executeToJSON(String scriptName, Map<String, Object> data) throws NyException {
        nyQLInstance.executeToJSON(scriptName, data)
    }

    /**
     * Executes the given script and returns the result as a json string.
     * <p>
     *     If you still want to parse the json again, use the other execute method
     *     <code>execute(String, Map)</code>.
     * </p>
     *
     * @param scriptName name of the script to run.
     * @return result as json string.
     * @throws NyException any exception thrown while executing and parsing.
     */
    @CompileStatic
    static String executeToJSON(String scriptName) throws NyException {
        nyQLInstance.executeToJSON(scriptName)
    }

    @CompileStatic
    private static boolean asBoolean(String text) {
        text != null && (text.equalsIgnoreCase(TRUE_STR) || text.equalsIgnoreCase('yes'))
    }
}
