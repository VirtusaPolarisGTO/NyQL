package com.virtusa.gto.insight.nyql.engine

import com.virtusa.gto.insight.nyql.configs.Configurations
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.QExecutorRegistry
import com.virtusa.gto.insight.nyql.model.QRepositoryRegistry
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QSession
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

    static {
        configure()

        Runtime.runtime.addShutdownHook(new Thread(new Runnable() {
            @Override
            void run() {
                shutdown()
            }
        }));
    }

    private static void configure() {
        if (!Configurations.instance().isConfigured()) {
            LOGGER.warn("NyQL is going to configure with default configurations using classpath...")
            File nyConfig = new File("nyql.json");
            if (!nyConfig.exists()) {
                LOGGER.error("No nyql.json file is found on classpath! [" + nyConfig.absolutePath + "]")
                throw new RuntimeException("No nyql.json file is found on classpath! [" + nyConfig.absolutePath + "]");
            } else {
                LOGGER.debug("Loading configurations from " + nyConfig.absolutePath + "...")
                Configurations.instance().configure(new JsonSlurper().parse(nyConfig) as Map)
            }

        } else {
            LOGGER.warn("NyQL has already been configured!")
        }
    }

    public static QScript parse(String scriptName) throws NyException {
        return parse(scriptName, EMPTY_MAP)
    }

    public static QScript parse(String scriptName, Map<String, Object> data) throws NyException {
        QSession qSession = QSession.create()
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
