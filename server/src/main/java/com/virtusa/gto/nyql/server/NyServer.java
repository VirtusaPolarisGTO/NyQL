package com.virtusa.gto.nyql.server;


import com.google.gson.Gson;
import com.virtusa.gto.nyql.engine.NyQL;
import com.virtusa.gto.nyql.exceptions.NyException;
import com.virtusa.gto.nyql.model.QScript;
import com.virtusa.gto.nyql.model.units.AParam;
import groovy.json.JsonSlurper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Gson GSON = new Gson();

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        // turn off jetty logging
        org.eclipse.jetty.util.log.Log.setLog(null);

        // read server config file
        File confFile = new File("./config/server.json");
        JsonSlurper jsonSlurper = new JsonSlurper();
        Map<String, Object> confObject = (Map<String, Object>) jsonSlurper.parse(confFile, StandardCharsets.UTF_8.name());

        File nyConfigFile = new File("./config/nyql.json");
        NyQL.configure(nyConfigFile);

        String basePath = confObject.get("basePath") != null ? String.valueOf(confObject.get("basePath")) : "";
        int port = (int) confObject.get("port");
        Spark.port(port);
        Runtime.getRuntime().addShutdownHook(new Thread(Spark::stop));
        Runtime.getRuntime().addShutdownHook(new Thread(NyQL::shutdown));


        Spark.webSocket(basePath + "/profile", NyProfileSocket.class);

        // register routes
        Spark.post(basePath + "/parse", (req, res) -> {
            res.type("application/json");

            Map<String, Object> bodyData = (Map<String, Object>) new JsonSlurper().parseText(req.body());
            String scriptId = String.valueOf(bodyData.get("scriptId"));
            Map<String, Object> data = new HashMap<>();
            if (bodyData.containsKey("data")) {
                data = (Map<String, Object>) bodyData.get("data");
            }
            QScript result = NyQL.parse(scriptId, data);

            Map<String, Object> r = new HashMap<>();
            r.put("query", result.getProxy().getQuery());
            r.put("params", result.getProxy().getOrderedParameters().stream()
                    .map(AParam::get__name).collect(Collectors.toList()));
            return r;
        }, GSON::toJson);

        Spark.post(basePath + "/execute", (req, res) -> {
            res.type("application/json");

            Map<String, Object> bodyData = (Map<String, Object>) new JsonSlurper().parseText(req.body());
            String scriptId = String.valueOf(bodyData.get("scriptId"));
            Map<String, Object> data = new HashMap<>();
            if (bodyData.containsKey("data")) {
                data = (Map<String, Object>) bodyData.get("data");
            }

            return NyQL.execute(scriptId, data);
        }, GSON::toJson);

        Spark.exception(NyException.class, (ex, req, res) -> {
            res.status(500);
            res.body("Script parsing or execution error!" + req.pathInfo());
        });

        LOGGER.debug("Server is running at " + port + "...");
    }

}
