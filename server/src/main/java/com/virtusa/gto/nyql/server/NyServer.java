package com.virtusa.gto.nyql.server;


import com.google.gson.Gson;
import com.virtusa.gto.nyql.exceptions.NyException;
import com.virtusa.gto.nyql.model.QScript;
import com.virtusa.gto.nyql.model.QScriptResult;
import com.virtusa.gto.nyql.model.units.AParam;
import groovy.json.JsonSlurper;
import javafx.util.Pair;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author IWEERARATHNA
 */
public class NyServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NyServer.class);

    private static final String DEF_SERVER_JSON = "./config/server.json";
    private static final String DEF_NYQL_JSON = "./config/nyql.json";

    private static final String CONTENT_JSON = "application/json";

    private static final Gson GSON = new Gson();

    private String basePath;

    private Map<String, Object> configs;
    private boolean authEnabled = true;
    private String appToken = null;

    private final NyInstancePool instancePool = new NyInstancePool();

    private NyServer() {
    }

    @SuppressWarnings("unchecked")
    private void init() throws Exception {
        String serverJson = readEnv("NYSERVER_CONFIG_PATH", DEF_SERVER_JSON);
        String nyqlJson = readEnv("NYSERVER_NYJSON_PATH", DEF_NYQL_JSON);

        // read server config file
        File confFile = new File(serverJson);
        JsonSlurper jsonSlurper = new JsonSlurper();
        Map<String, Object> confObject = (Map<String, Object>) jsonSlurper.parse(confFile, StandardCharsets.UTF_8.name());

        configs = confObject;
        Map<String, Object> authConfigs = (Map<String, Object>) confObject.get("auth");
        authEnabled = Boolean.parseBoolean(authConfigs.get("enabled").toString());
        appToken = readEnv("NYSERVER_AUTH_TOKEN", authConfigs.get("token").toString());
        basePath = confObject.get("basePath") != null ? String.valueOf(confObject.get("basePath")) : "";
        if (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        int port = Integer.parseInt(readEnv("NYSERVER_PORT", confObject.get("port").toString()));

        // load all specified database instances...
        Pair<String, Map<String, File>> instanceMap = loadDBInstances(confFile, confObject);
        String defInst = instanceMap.getKey();
        Map<String, File> map = instanceMap.getValue();
        if (defInst == null) {
            throw new IOException("A default database instance is not set! Server cannot start without a default instance!");
        }

        for (Map.Entry<String, File> entry : map.entrySet()) {
            instancePool.loadNyQL(entry.getKey(), entry.getValue());
        }
        instancePool.setDefaultInstance(defInst);


        Spark.port(port);

        // register spark server stop hook
        Runtime.getRuntime().addShutdownHook(new Thread(Spark::stop));

        // initialize all nyql instances
        instancePool.init();

        // register all routes...
        registerRoutes();
        LOGGER.info("Server is running at " + port + "...");
    }

    @SuppressWarnings("unchecked")
    private Pair<String, Map<String, File>> loadDBInstances(File serverJson, Map<String, Object> confObject) throws IOException {
        Map<String, File> configMap = new HashMap<>();
        String defInst = readEnv("NYSERVER_DEFAULT_INSTANCE", null);
        boolean loaded = false;

        File wdir = new File(".").getCanonicalFile();
        File srvDir = serverJson.getParentFile().getCanonicalFile();

        String nyqlJsons = readEnv("NYSERVER_NYJSON_PATHS", null);
        if (nyqlJsons != null) {
            LOGGER.debug("Loading multiple instances from environment...");

            // expected format:
            //    NYSERVER_NYJSON_PATHS=mainDB:./nyql-main.json,secondaryDB:./nyql-sec.json
            //
            // load from env
            String[] items = nyqlJsons.split("[,]");
            for (String item : items) {
                int pos = item.indexOf(':');
                if (pos > 0) {
                    String instName = item.substring(0, pos).trim();
                    String loc = item.substring(pos + 1).trim();
                    configMap.put(instName, existFirst(loc, srvDir, wdir));

                } else {
                    throw new IOException("Instance name is not defined for one of nyql configuration paths! [" + item + "]");
                }
            }
            loaded = true;
        }

        String nyqlJson = readEnv("NYSERVER_NYJSON_PATH", null);
        if (nyqlJson != null) {
            LOGGER.debug("Loading single instance from environment...");

            defInst = NyInstancePool.DEF_INSTANCE;
            configMap.put(defInst, existFirst(nyqlJson, srvDir, wdir));
            loaded = true;
        }

        if (!loaded) {
            LOGGER.debug("Loading instances from server.json file...");
            // load from server.json...

            List<?> instArray = (List) confObject.get("instances");
            if (instArray != null && instArray.size() > 0) {
                for (Object item : instArray) {
                    Map<String, Object> data = (Map<String, Object>) item;

                    String instName = (String) data.get("instanceName");
                    String path = (String) data.get("configJson");
                    boolean isDef = (Boolean) data.getOrDefault("default", false);

                    if (instName == null || path == null) {
                        throw new IOException("Either key 'instanceName' or 'configJson' is missing for one of instance definition!");
                    }

                    configMap.put(instName, existFirst(path, srvDir, wdir));
                    if (isDef) {
                        defInst = instName;
                    }
                }
            }
        }

        return new Pair<>(defInst, configMap);
    }

    private static File existFirst(String path, File... dirs) throws IOException {
        for (File f : dirs) {
            File tmp = new File(f, path);
            if (tmp.exists()) {
                return tmp.getCanonicalFile();
            }
        }
        throw new IOException("Given file " + path + " does not exist in any possible locations!");
    }

    private void registerRoutes() {
        if ((boolean)configs.getOrDefault("websocket", true)) {
            Spark.webSocket(basePath + "/profile", NyProfileSocket.class);
        }

        Spark.before((request, response) -> checkAuthHeader(request));

        Spark.post(basePath + "/parse", this::epParse, GSON::toJson);
        Spark.post(basePath + "/execute", this::epExecute, GSON::toJson);

        // handle exceptions
        Spark.exception(NyException.class, this::anyError);
    }

    private void checkAuthHeader(Request request) throws NyServerAuthException {
        if (!authEnabled) {
            return;
        }
        String header = request.headers("Authorization");
        if (header == null) {
            throw new NyServerAuthException();
        } else {
            if (header.startsWith("Bearer ")) {
                header = header.substring(7);
            }
            if (!header.equals(appToken)) {
                throw new NyServerAuthException();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object epParse(Request req, Response res) throws Exception {
        res.type(CONTENT_JSON);

        Map<String, Object> bodyData = (Map<String, Object>) new JsonSlurper().parseText(req.body());
        String scriptId = String.valueOf(bodyData.get("scriptId"));
        String instance = String.valueOf(bodyData.get("instance"));
        Map<String, Object> data = new HashMap<>();
        if (bodyData.containsKey("data")) {
            data = (Map<String, Object>) bodyData.get("data");
        }
        QScript result = instancePool.getInstance(instance).parse(scriptId, data);

        Map<String, Object> r = new HashMap<>();
        if (result instanceof QScriptResult) {
            r.put("result", ((QScriptResult) result).getScriptResult());
            r.put("query", null);
            r.put("params", null);
        } else {
            r.put("result", null);
            r.put("query", result.getProxy() == null ? null : result.getProxy().getQuery());
            r.put("params", result.getProxy().getOrderedParameters().stream()
                    .map(AParam::get__name).collect(Collectors.toList()));
        }
        return r;
    }

    @SuppressWarnings("unchecked")
    private Object epExecute(Request req, Response res) throws Exception {
        res.type(CONTENT_JSON);

        Map<String, Object> bodyData = (Map<String, Object>) new JsonSlurper().parseText(req.body());
        String scriptId = String.valueOf(bodyData.get("scriptId"));
        String instance = String.valueOf(bodyData.get("instance"));
        Map<String, Object> data = (Map<String, Object>) bodyData.getOrDefault("data", new HashMap<>());

        Map<String, Object> r = new HashMap<>();
        try {
            Object sqlResult = instancePool.getInstance(instance).execute(scriptId, data);
            r.put("result", sqlResult);
        } catch (Throwable t) {
            LOGGER.error("Error occurred executing script! " + scriptId, t);
            throw new NyException("Error occurred while script running!", t);
        }

        return r;
    }

    private void anyError(Exception ex, Request req, Response res) {
        if (ex instanceof NyServerAuthException) {
            Spark.halt(401, ex.getMessage());
        } else {
            res.status(500);
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        // turn off jetty logging
        org.eclipse.jetty.util.log.Log.setLog(null);

        String log4jConf = readEnv("LOG4J_CONFIG_FILE", "./config/log4j.properties");
        PropertyConfigurator.configure(log4jConf);

        printLogo();

        NyServer server = new NyServer();
        server.init();
    }

    private static void printLogo() {
        String ver = readEnv("com.virtusa.gto.nyql.version", readVersion());
        System.out.println("\t  _  _        ___                          \n" +
                "\t | \\| | _  _ / __| ___  _ _ __ __ ___  _ _ \n" +
                "\t | .` || || |\\__ \\/ -_)| '_|\\ V // -_)| '_|\n" +
                "\t |_|\\_| \\_, ||___/\\___||_|   \\_/ \\___||_|  \n" +
                "\t        |__/                               ");
        System.out.println("                                     - NyQL v" + ver);
        System.out.println();
    }

    private static String readVersion() {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("nyql_buildinfo.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty("nyql.version", "[UNKNOWN]");

        } catch (IOException ex) {
            LOGGER.error("Unable to determine NyQL version!", ex);
            return "[UNKNOWN]";
        }
    }

    static String readEnv(String key, String defValue) {
        String val = System.getProperty(key, System.getenv(key));
        if (val == null) {
            return defValue;
        } else {
            return val;
        }
    }
}
