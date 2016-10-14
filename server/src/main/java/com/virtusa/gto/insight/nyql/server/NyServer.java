package com.virtusa.gto.insight.nyql.server;


import com.virtusa.gto.insight.nyql.engine.NyQL;
import groovy.json.JsonSlurper;
import spark.Spark;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author IWEERARATHNA
 */
public class NyServer {

    public static void main(String[] args) {
        org.eclipse.jetty.util.log.Log.setLog(null);

        // read server config file
        File confFile = new File("./config/server.json");
        JsonSlurper jsonSlurper = new JsonSlurper();
        Map<String, Object> confObject = (Map<String, Object>) jsonSlurper.parse(confFile, StandardCharsets.UTF_8.name());

        File nyConfigFile = new File("./config/nyql.json");
        NyQL.configure(nyConfigFile);

        int port = (int) confObject.get("port");
        Spark.port(port);
        Spark.get("/hello", (req, res) -> "Hello world!");
        System.out.println("Server is running at " + port + "...");
        Runtime.getRuntime().addShutdownHook(new Thread(Spark::stop));
    }

}
