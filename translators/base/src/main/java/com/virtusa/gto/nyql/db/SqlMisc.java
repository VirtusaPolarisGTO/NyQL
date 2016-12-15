package com.virtusa.gto.nyql.db;

import groovy.json.JsonSlurper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author IWEERARATHNA
 */
public class SqlMisc {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlMisc.class);

    @SuppressWarnings("unchecked")
    private static Collection<String> loadKeywords(InputStream inputStream) {
        Object parse = new JsonSlurper().parse(inputStream, StandardCharsets.UTF_8.name());
        return (List) parse;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadNameMappings(InputStream inputStream) {
        Object parse = new JsonSlurper().parse(inputStream, StandardCharsets.UTF_8.name());
        return (Map) parse;
    }

    public static Map<String, Object> loadNameMappings(String resourcePath, File keywordFileLocation) throws IOException {
        InputStream inputStream = null;
        Map<String, Object> map = new HashMap<>();
        try {
            if (keywordFileLocation != null && keywordFileLocation.exists()) {
                LOGGER.debug("Loading name mappings from " + keywordFileLocation);
                inputStream = new FileInputStream(keywordFileLocation);
            } else {
                LOGGER.debug("Loading name mappings from classpath " + resourcePath);
                inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            }

            if (inputStream != null) {
                map.putAll(loadNameMappings(inputStream));
            } else {
                LOGGER.warn("Could not load name mappings from classpath!");
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return map;
    }

    public static Set<String> loadKeywords(String resourcePath, File keywordFileLocation) throws IOException {
        InputStream inputStream = null;
        Set<String> klist = new HashSet<>();
        try {
            if (keywordFileLocation != null && keywordFileLocation.exists()) {
                LOGGER.debug("Loading keywords from " + keywordFileLocation);
                inputStream = new FileInputStream(keywordFileLocation);
            } else {
                LOGGER.debug("Loading keywords from classpath " + resourcePath);
                inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            }

            if (inputStream != null) {
                klist.addAll(loadKeywords(inputStream));
            } else {
                LOGGER.warn("Could not load reserved keyword list from classpath!");
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return klist;
    }

}
