package com.virtusa.gto.nyql.server;


import com.google.gson.Gson;
import com.virtusa.gto.nyql.engine.NyQL;
import com.virtusa.gto.nyql.exceptions.NyException;
import com.virtusa.gto.nyql.model.QScript;
import com.virtusa.gto.nyql.model.QScriptResult;
import com.virtusa.gto.nyql.model.units.AParam;
import groovy.json.JsonSlurper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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

    private NyServer() {
    }

    @SuppressWarnings("unchecked")
    private void init() {
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

        File nyConfigFile = new File(nyqlJson);
        NyQL.configure(nyConfigFile);

        // register nyql shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(NyQL::shutdown));

        Spark.port(port);

        // register spark server stop hook
        Runtime.getRuntime().addShutdownHook(new Thread(Spark::stop));

        // register all routes...
        registerRoutes();
        LOGGER.debug("Server is running at " + port + "...");
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
        Map<String, Object> data = new HashMap<>();
        if (bodyData.containsKey("data")) {
            data = (Map<String, Object>) bodyData.get("data");
        }
        QScript result = NyQL.parse(scriptId, data);

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
        Map<String, Object> data = (Map<String, Object>) bodyData.getOrDefault("data", new HashMap<>());

        Map<String, Object> r = new HashMap<>();
        r.put("result", NyQL.execute(scriptId, data));
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
    public static void main(String[] args) {
        // turn off jetty logging
        org.eclipse.jetty.util.log.Log.setLog(null);

        printLogo();

        NyServer server = new NyServer();
        server.init();
    }

    private static void printLogo() {
        String ver = readEnv("com.virtusa.gto.nyql.version", "1.1");
        System.out.println("\t  _  _        ___                          \n" +
                "\t | \\| | _  _ / __| ___  _ _ __ __ ___  _ _ \n" +
                "\t | .` || || |\\__ \\/ -_)| '_|\\ V // -_)| '_|\n" +
                "\t |_|\\_| \\_, ||___/\\___||_|   \\_/ \\___||_|  \n" +
                "\t        |__/                               ");
        System.out.println("                                     - NyQL v" + ver);
        System.out.println();
    }

    private static String readEnv(String key, String defValue) {
        String val = System.getProperty(key, System.getenv(key));
        if (val == null) {
            return defValue;
        } else {
            return val;
        }
    }
}
