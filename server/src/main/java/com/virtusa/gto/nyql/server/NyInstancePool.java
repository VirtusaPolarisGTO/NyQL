package com.virtusa.gto.nyql.server;

import com.virtusa.gto.nyql.engine.NyQLInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
class NyInstancePool {

    private static final Logger LOGGER = LoggerFactory.getLogger(NyInstancePool.class);

    static String DEF_INSTANCE = "DEFAULT";

    private final Map<String, NyQLInstance> nyQLInstanceMap = new HashMap<>();
    private NyQLInstance defInstance;
    private NyQLInstance recentInstance;

    NyInstancePool() {
    }

    void loadNyQL(String instanceName, File configJson) {
        LOGGER.debug("Initializing database instance '" + instanceName + "' from file " + configJson + "...");
        NyQLInstance nyQLInstance = NyQLInstance.create(configJson);

        // register nyql shutdown hook
        LOGGER.debug("  - Registering shutdown hook...");
        Runtime.getRuntime().addShutdownHook(new Thread(nyQLInstance::shutdown));

        nyQLInstanceMap.put(instanceName, nyQLInstance);
        recentInstance = nyQLInstance;
        LOGGER.debug("Successfully initialized instance '" + instanceName + "'!");
    }

    NyInstancePool setDefaultInstance(String instanceName) throws IOException {
        LOGGER.debug("Database connection '" + instanceName + "' is made as the default!");
        defInstance = getInstance(instanceName);
        return this;
    }

    void init() throws Exception {
        LOGGER.debug("--- Running for Liquibase changelogs ---");
        for (Map.Entry<String, NyQLInstance> entry : nyQLInstanceMap.entrySet()) {
            LOGGER.debug("  - " + entry.getKey());
            new NyChangeLog(entry.getValue()).execute();
        }
    }

    NyQLInstance getInstance(String instanceName) throws IOException {
        if (instanceName == null || instanceName.length() == 0) {
            return defInstance;
        }

        NyQLInstance qlInstance = nyQLInstanceMap.get(instanceName);
        if (qlInstance != null) {
            return qlInstance;
        } else {
            throw new IOException("There is no instance loaded by name '" + instanceName + "'!");
        }
    }

}
