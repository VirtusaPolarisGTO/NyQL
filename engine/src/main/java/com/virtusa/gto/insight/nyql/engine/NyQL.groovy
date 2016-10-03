package com.virtusa.gto.insight.nyql.engine

import com.virtusa.gto.insight.nyql.configs.Configurations
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.QExecutorRegistry
import com.virtusa.gto.insight.nyql.model.QRepositoryRegistry
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QSession
import com.virtusa.gto.insight.nyql.utils.Constants
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Main interface to interact with NyQL queries.
 *
 * @author IWEERARATHNA
 */
class NyQL {

    private static final Logger LOGGER = LoggerFactory.getLogger(NyQL.class);

    private static final Map<String, Object> EMPTY_MAP = new HashMap<>();

    private static final String JSON_CONFIG_FILENAME = "nyql.json";

    static {
        if (!Boolean.parseBoolean(System.getProperty("nyql.autoConfig", "true"))) {
            LOGGER.warn("*"*100)
            LOGGER.warn("You MUST EXPLICITLY Call Configure with nyql configuration json file!")
            LOGGER.warn("*"*100)
            return;
        }

        configure()

        if (Configurations.instance().addShutdownHook()) {
            LOGGER.warn("Automatically adding a NyQL shutdown hook...")
            Runtime.runtime.addShutdownHook(new Thread(new Runnable() {
                @Override
                void run() {
                    shutdown()
                }
            }));
        } else {
            LOGGER.warn("*"*100)
            LOGGER.warn("You MUST EXPLICITLY Call SHUTDOWN method of NyQL when you are done with this!")
            LOGGER.warn("*"*100)
        }
    }

    public static void configure(File inputJson=null, boolean force=false) {
        if (!Configurations.instance().isConfigured() || force) {
            LOGGER.warn("NyQL is going to configure with default configurations using classpath...")
            File nyConfig = inputJson ?: new File(JSON_CONFIG_FILENAME);
            if (!nyConfig.exists()) {
                LOGGER.error("*"*100)
                LOGGER.error("No nyql.json file is found on classpath! [" + nyConfig.absolutePath + "]")
                LOGGER.error(" "*50)
                LOGGER.error("Explicitly call the configure method with configuration input file!")
                LOGGER.error("*"*100)
                //throw new RuntimeException("No '$JSON_CONFIG_FILENAME' file is found on classpath! [" + nyConfig.absolutePath + "]");
            } else {
                LOGGER.debug("Loading configurations from " + nyConfig.absolutePath + "...")
                Map configData = new JsonSlurper().parse(nyConfig) as Map
                configData.put("_location", new File(".").absolutePath)
                Configurations.instance().configure(configData)
            }

        } else {
            LOGGER.warn("NyQL has already been configured!")
        }
    }

    public static QScript parse(String scriptName) throws NyException {
        return parse(scriptName, EMPTY_MAP)
    }

    public static QScript parse(String scriptName, Map<String, Object> data) throws NyException {
        QSession qSession = QSession.create(scriptName)
        if (data) {
            qSession.sessionVariables.putAll(data)
        }
        return QRepositoryRegistry.instance.defaultRepository().parse(scriptName, qSession)
    }

    public static void shutdown() {
        Configurations.instance().shutdown()
    }

    public static Object execute(String scriptName) throws NyException {
        return execute(scriptName, EMPTY_MAP);
    }

    public static Object execute(String scriptName, Map<String, Object> data) throws NyException {
        return QExecutorRegistry.instance.defaultExecutorFactory().create().execute(parse(scriptName, data))
    }

    public static String executeToJSON(String scriptName, Map<String, Object> data) throws NyException {
        Object result = execute(scriptName, data);
        return JsonOutput.toJson(result);
    }

}
