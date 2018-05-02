package com.virtusa.gto.nyql.engine;

import com.virtusa.gto.nyql.configs.ConfigBuilder;
import com.virtusa.gto.nyql.configs.ConfigKeys;
import com.virtusa.gto.nyql.configs.ConfigParser;
import com.virtusa.gto.nyql.configs.Configurations;
import com.virtusa.gto.nyql.configs.JmxConfigurator;
import com.virtusa.gto.nyql.engine.exceptions.NyScriptExecutionException;
import com.virtusa.gto.nyql.engine.impl.NyQLResult;
import com.virtusa.gto.nyql.exceptions.NyConfigurationException;
import com.virtusa.gto.nyql.exceptions.NyException;
import com.virtusa.gto.nyql.model.NyQLInstanceMXBean;
import com.virtusa.gto.nyql.model.QExecutor;
import com.virtusa.gto.nyql.model.QPagedScript;
import com.virtusa.gto.nyql.model.QScript;
import com.virtusa.gto.nyql.model.QSession;
import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import groovy.transform.CompileStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
/**
 * @author IWEERARATHNA
 */
public class NyQLInstance implements NyQLInstanceMXBean, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NyQLInstance.class);
    
    private static final Map<String, Object> EMPTY_MAP = new HashMap<>();

    private final Configurations configurations;

    private NyQLInstance(Configurations theConfigInstance) {
        this.configurations = theConfigInstance;
    }

    public static NyQLInstance createFromResource(String name, String resourcePath) throws NyException {
        return createFromResource(name, resourcePath, null);
    }

    public static NyQLInstance createFromResource(String name, String resourcePath, ClassLoader classLoader) throws NyException {
        ClassLoader cl = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = cl.getResourceAsStream(resourcePath)) {
            return create(name, inputStream);
        } catch (Exception ex) {
            throw new NyConfigurationException("Failed to read resource '${resourcePath}'!", ex);
        }
    }

    @Deprecated
    public static NyQLInstance create(InputStream inputStream) throws NyConfigurationException {
        return create(null, inputStream);
    }

    public static NyQLInstance create(String name, InputStream inputStream) throws NyConfigurationException {
        return create(name, ConfigParser.parseAndResolve(inputStream));
    }

    @Deprecated
    public static NyQLInstance create(File configFile) throws NyConfigurationException {
        return create(null, configFile);
    }

    public static NyQLInstance create(String name, File configFile) throws NyConfigurationException {
        return create(name, ConfigParser.parseAndResolve(configFile));
    }

    public static NyQLInstance create(String name, Map<String, Object> configData) throws NyConfigurationException {
        try {
            configData.put(ConfigKeys.LOCATION_KEY, new File(".").getCanonicalPath());
            Configurations configInst = ConfigBuilder.instance(name).setupFrom(configData).build();
            return create(configInst);
        } catch (IOException e) {
            throw new NyConfigurationException("Failed to identify current working directory for app!", e);
        }
    }

    public static NyQLInstance create(Configurations configInst) throws NyConfigurationException {
        NyQLInstance nyQLInstance = new NyQLInstance(configInst);
        if (configInst.isRegisterMXBeans()) {
            JmxConfigurator.get().registerMXBean(nyQLInstance);
        }
        return nyQLInstance;
    }

    public Configurations getConfigurations() {
        return configurations;
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
     * @throws com.virtusa.gto.nyql.exceptions.NyException any exception thrown while parsing.
     */
    @CompileStatic
    public QScript parse(String scriptName) throws NyException {
        return parse(scriptName, EMPTY_MAP);
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
    public QScript parse(String scriptName, Map<String, Object> data) throws NyException {
        QSession qSession = QSession.create(configurations, scriptName);
        if (data != null) {
            qSession.getSessionVariables().putAll(data);
        }
        return configurations.getRepositoryRegistry().defaultRepository().parse(scriptName, qSession);
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
    public void recompileScript(String scriptName) throws NyException {
        configurations.getRepositoryRegistry().defaultRepository().reloadScript(scriptName);
    }

    /**
     * Shutdown the nyql engine.
     * This should be called only when your application exits.
     */
    public void shutdown() {
        if (configurations.isRegisterMXBeans()) {
            JmxConfigurator.get().removeMXBean(this);
        }
        configurations.shutdown();
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
    @SuppressWarnings("unchecked")
    @CompileStatic
    public <T> T execute(String scriptName) throws NyException {
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
    @SuppressWarnings("unchecked")
    @CompileStatic
    public <T> T execute(String scriptName, Map<String, Object> data) throws NyException {
        QScript script = null;
        try {
            script = parse(scriptName, data);
            return (T) configurations.getExecutorRegistry().defaultExecutorFactory().create().execute(script);
        } catch (Exception ex) {
            if (ex instanceof NyException) {
                throw (NyException) ex;
            } else {
                throw new NyScriptExecutionException("Ny script execution error!", ex);
            }
        } finally {
            if (script != null) {
                script.free();
            }
        }
    }

    /**
     * Executes the given <code>select</code> query and fetches subset of result each has rows size of
     * <code>pageSize</code>. This query will run only once in the server and the result rows are
     * paginated.
     * 
     * <p>
     *     <b>Caution:</b> This execution is NOT equivalent to the db cursors, but this is
     *     a JDBC level pagination which makes easier for developers to iterate subset of
     *     results efficiently from code rather not loading all result rows into the application
     *     memory at once.
     * </p>
     *
     * @param scriptName name of the script to run.
     * @param pageSize number of rows per page to return in each block.
     * @param data set of variables to be passed to the script run.
     * @return an iterable list of pages (blocks) of rows. The last page may not have <code>pageSize</code> rows,
     *          but at least one.
     * @throws NyException any exception thrown while executing for pagination. This may cause the provided
     *          script has none-other than SELECT query type.
     */
    @SuppressWarnings("unchecked")
    @CompileStatic
    public Iterable<NyQLResult> paginate(String scriptName, int pageSize, Map<String, Object> data) throws NyException {
        QScript script = new QPagedScript(parse(scriptName, data), pageSize);
        try {
            return (Iterable<NyQLResult>) configurations.getExecutorRegistry().defaultExecutorFactory().create().execute(script);
        } catch (Exception ex) {
            if (ex instanceof NyException) {
                throw (NyException) ex;
            } else {
                throw new NyScriptExecutionException("Ny script execution error!", ex);
            }
        }
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
    public String executeToJSON(String scriptName, Map<String, Object> data) throws NyException {
        Object result = execute(scriptName, data);
        if (result == null) {
            return null;
        } else {
            return JsonOutput.toJson(result);
        }
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
    public String executeToJSON(String scriptName) throws NyException {
        return executeToJSON(scriptName, new HashMap<>());
    }

    /**
     * Programmatically (using API) do some sequence of operations inside a transaction. This would
     * be useful, specially if you don't want to script your transaction logic externally. In case
     * of an exception, transaction will be rollback automatically, but will throw the exception.
     *
     * Always use the provided nyql instance to execute scripts at all.
     *
     * @param transactionName a unique id for this executing transaction.
     * @param body the content of transaction.
     * @param data data for the transaction content.
     * @param autoCommit should do auto commit
     * @throws NyException any exception thrown while transaction.
     */
    @CompileStatic
    public <T> T doTransaction(String transactionName, BiFunction<NyQLInstance, Map<String, Object>, T> body,
                        Map<String, Object> data, boolean autoCommit) throws NyException {
        QExecutor executor = null;
        try (QSession qSession = QSession.create(configurations, transactionName)) {
            executor = qSession.getExecutor();
            executor.startTransaction();
            T result = body.apply(this, data);
            if (autoCommit) {
                executor.commit();
            }
            return result;

        } catch (Exception ex) {
            if (executor != null) {
                executor.rollback(null);
            }
            throw new NyException("An exception occurred inside transaction '$transactionName'!", ex);
        } finally {
            if (executor != null) {
                executor.done();
            }
        }
    }

    @Override
    public void close() {
        shutdown();
    }

    // *******************************************************************
    //          JMX Methods
    // *******************************************************************

    @Override
    public String getName() {
        return configurations.getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public String executeToJSON(String scriptName, String dataJson) {
        try {
            Map<String, Object> jsonMap = (Map<String, Object>) new JsonSlurper().parseText(dataJson);
            return executeToJSON(scriptName, jsonMap);
        } catch (Exception ex) {
            LOGGER.error("Error occurred while executing script through JMX interface!", ex);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public String parseScript(String scriptName, String dataJson) {
        try {
            Map<String, Object> jsonMap = (Map<String, Object>) new JsonSlurper().parseText(dataJson);
            QScript script = parse(scriptName, jsonMap);
            return script.toString();
        } catch (Exception ex) {
            LOGGER.error("Error occurred while parsing script through JMX interface!", ex);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void recompile(String scriptName) {
        try {
            recompileScript(scriptName);
        } catch (Exception ex) {
            LOGGER.error("Error occurred while recompiling script through JMX interface!", ex);
        }
    }
}
