package com.virtusa.gto.nyql.engine

import com.virtusa.gto.nyql.configs.ConfigBuilder
import com.virtusa.gto.nyql.configs.ConfigKeys
import com.virtusa.gto.nyql.configs.ConfigParser
import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QSession
import groovy.json.JsonOutput
import groovy.transform.CompileStatic
/**
 * @author IWEERARATHNA
 */
@CompileStatic
class NyQLInstance {
    
    private static final Map<String, Object> EMPTY_MAP = [:]

    private final Configurations configurations

    private NyQLInstance(Configurations theConfigInstance) {
        this.configurations = theConfigInstance
    }

    static NyQLInstance create(InputStream inputStream) {
        create(ConfigParser.parseAndResolve(inputStream))
    }

    static NyQLInstance create(File configFile) {
        create(ConfigParser.parseAndResolve(configFile))
    }

    static NyQLInstance create(Map configData) {
        configData.put(ConfigKeys.LOCATION_KEY, new File('.').canonicalPath)
        Configurations configInst = ConfigBuilder.instance().setupFrom(configData).build()
        create(configInst)
    }

    static NyQLInstance create(Configurations configInst) {
        new NyQLInstance(configInst)
    }

    Configurations getConfigurations() {
        return configurations
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
    QScript parse(String scriptName) throws NyException {
        parse(scriptName, EMPTY_MAP)
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
    QScript parse(String scriptName, Map<String, Object> data) throws NyException {
        QSession qSession = QSession.create(configurations, scriptName)
        if (data) {
            qSession.sessionVariables.putAll(data)
        }
        configurations.repositoryRegistry.defaultRepository().parse(scriptName, qSession)
    }

    /**
     * Shutdown the nyql engine.
     * This should be called only when your application exits.
     */
    void shutdown() {
        configurations.shutdown()
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
    <T> T execute(String scriptName) throws NyException {
        (T) execute(scriptName, EMPTY_MAP)
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
    <T> T execute(String scriptName, Map<String, Object> data) throws NyException {
        QScript script = null
        try {
            script = parse(scriptName, data)
            (T) configurations.executorRegistry.defaultExecutorFactory().create().execute(script)
        } finally {
            if (script != null) {
                script.free()
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
    String executeToJSON(String scriptName, Map<String, Object> data) throws NyException {
        Object result = execute(scriptName, data)
        if (result == null) {
            null
        } else {
            JsonOutput.toJson(result)
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
    String executeToJSON(String scriptName) throws NyException {
        executeToJSON(scriptName, [:])
    }
}
