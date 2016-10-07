package com.virtusa.gto.insight.nyql.engine

import com.virtusa.gto.insight.nyql.DSLContext
import com.virtusa.gto.insight.nyql.configs.ConfigBuilder
import com.virtusa.gto.insight.nyql.configs.Configurations
import com.virtusa.gto.insight.nyql.engine.impl.QExternalJdbcFactory
import com.virtusa.gto.insight.nyql.engine.impl.QJdbcExecutor
import com.virtusa.gto.insight.nyql.engine.repo.QSingleScript
import com.virtusa.gto.insight.nyql.engine.repo.QSingleScriptRepository
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.*
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets
import java.sql.Connection

/**
 * Main interface to interact with NyQL queries.
 *
 * @author IWEERARATHNA
 */
class NyQL {

    private static final Logger LOGGER = LoggerFactory.getLogger(NyQL.class);

    private static final Map<String, Object> EMPTY_MAP = [:];

    private static final String BOOTSTRAP_KEY = 'com.virtusa.gto.insight.nyql.autoBootstrap'
    private static final String LOAD_CLASSPATH_KEY = 'com.virtusa.gto.insight.nyql.classpathBootstrap'
    private static final String TRUE_STR = 'true'
    private static final String JSON_CONFIG_FILENAME = 'nyql.json';

    static {
        if (!Boolean.parseBoolean(System.getProperty(BOOTSTRAP_KEY, TRUE_STR))) {
            LOGGER.warn("*"*100)
            LOGGER.warn('You MUST EXPLICITLY setup NyQL with programmatically or configuration json file!')
            LOGGER.warn("*"*100)
            return;
        }

        configure()

        if (Configurations.instance().addShutdownHook()) {
            LOGGER.warn('Automatically adding a NyQL shutdown hook...')
            Runtime.runtime.addShutdownHook(new Thread(new Runnable() {
                @Override
                void run() {
                    shutdown()
                }
            }));
        } else {
            LOGGER.warn("*"*100)
            LOGGER.warn('You MUST EXPLICITLY Call SHUTDOWN method of NyQL when you are done with this!')
            LOGGER.warn("*"*100)
        }
    }

    /**
     * Configure NyQL with a configuration json file. If the input json file is <code>null</code>,
     * then it will look for a file in classpath/working directory.
     *
     * @param inputJson input json file.
     * @param force if specified true, then configurations will be reloaded.
     */
    public static void configure(File inputJson=null, boolean force=false) {
        if (!Configurations.instance().isConfigured() || force) {
            LOGGER.warn('NyQL is going to configure with default configurations using classpath...')
            File nyConfig = inputJson ?: new File(JSON_CONFIG_FILENAME);
            if (!nyConfig.exists()) {
                if (!configFromClasspath()) {
                    LOGGER.error("*" * 100)
                    LOGGER.error("No nyql.json file is found on classpath! [${nyConfig.absolutePath}]")
                    LOGGER.error(" " * 50)
                    LOGGER.error('Explicitly call the configure method with configuration input file!')
                    LOGGER.error("*" * 100)
                }
                //throw new RuntimeException("No '$JSON_CONFIG_FILENAME' file is found on classpath! [" + nyConfig.absolutePath + "]");
            } else {
                LOGGER.debug("Loading configurations from ${nyConfig.canonicalPath}...")
                Map configData = new JsonSlurper().parse(nyConfig) as Map
                configData.put('_location', new File('.').canonicalPath)
                ConfigBuilder.instance().setupFrom(configData).build()
            }

        } else {
            LOGGER.warn('NyQL has already been configured!')
        }
    }

    public static boolean hasConfigured() {
        return Configurations.instance().configured
    }

    /**
     * Config NyQL from classpath.
     *
     * @return true if successfully configured from
     */
    private static boolean configFromClasspath() {
        if (!asBoolean(System.getProperty(LOAD_CLASSPATH_KEY, TRUE_STR))) {
            LOGGER.warn('NyQL configuration from classpath has been disabled!')
            return false
        }

        def res = Thread.currentThread().contextClassLoader.getResourceAsStream(JSON_CONFIG_FILENAME)
        if (res != null) {
            LOGGER.debug('Loading configurations from classpath...')
            Map configData = new JsonSlurper().parse(res, StandardCharsets.UTF_8.name()) as Map
            ConfigBuilder.instance().setupFrom(configData).build()
        } else {
            return false
        }
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
    public static QScript parse(String scriptName) throws NyException {
        return parse(scriptName, EMPTY_MAP)
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
    public static QScript parse(String scriptName, Map<String, Object> data) throws NyException {
        QSession qSession = QSession.create(scriptName)
        if (data) {
            qSession.sessionVariables.putAll(data)
        }
        return QRepositoryRegistry.instance.defaultRepository().parse(scriptName, qSession)
    }

    /**
     * Shutdown the nyql engine.
     * This should be called only when your application exits.
     */
    public static void shutdown() {
        Configurations.instance().shutdown()
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
    public static <T> T execute(String scriptName) throws NyException {
        return (T) execute(scriptName, EMPTY_MAP);
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
    public static <T> T execute(String scriptName, Map<String, Object> data) throws NyException {
        return (T) QExecutorRegistry.instance.defaultExecutorFactory().create().execute(parse(scriptName, data))
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
    public static <T> T execute(String dslSql, Map<String, Object> data, Connection connection) throws NyException {
        String scriptId = String.valueOf(System.currentTimeMillis())
        return execute(scriptId, dslSql, data, connection)
    }

    /**
     * <p>
     * Executes a script by providing its content and a jdbc connection at the runtime.
     * The given connection will be used to execute the script. </p>
     * <b>WARNING:</b>
     * There is no guarantee that it will cache your given script if you invoked this
     * with multiple times with the same content, since it is all about caching in <code>GroovyShell</code>.
     * NyQL <b>does not do</b> any caching at this level and unable to do so.
     *
     * @param scriptId a unique script id for.
     * @param dslSql dsl script content.
     * @param data set of variables required for the script.
     * @param connection jdbc connection to execute after parsing the script.
     * @return the result of the script execution.
     * @throws NyException any exception thrown while executing or parsing.
     */
    public static <T> T execute(String scriptId, String dslSql, Map<String, Object> data, Connection connection) throws NyException {
        QJdbcExecutor jdbcExecutor = new QJdbcExecutor(connection)
        QSingleScript qSingleScript = new QSingleScript(scriptId, dslSql)
        QRepository repository = new QSingleScriptRepository(qSingleScript)
        QSession session = QSession.createSession(DSLContext.getActiveDSLContext(),
                            repository, jdbcExecutor, new QExternalJdbcFactory(connection))

        session.sessionVariables.putAll(data ?: [:])
        QScript script = repository.parse(scriptId, session)
        return (T) jdbcExecutor.execute(script)
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
    public static String executeToJSON(String scriptName, Map<String, Object> data) throws NyException {
        Object result = execute(scriptName, data);
        return JsonOutput.toJson(result);
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
    public static String executeToJSON(String scriptName) throws NyException {
        return executeToJSON(scriptName, [:])
    }

    private static boolean asBoolean(String text) {
        text != null && (text.equalsIgnoreCase(TRUE_STR) || text.equalsIgnoreCase('yes'))
    }
}
