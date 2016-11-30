package com.virtusa.gto.nyql.db;

import groovy.json.JsonSlurper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public static Set<String> loadKeywords(String resourcePath, File keywordFileLocation) throws IOException {
        InputStream inputStream = null;
        Set<String> klist = new HashSet<>();
        try {
            if (keywordFileLocation != null && keywordFileLocation.exists()) {
                LOGGER.debug("Loading mysql keywords from " + keywordFileLocation);
                inputStream = new FileInputStream(keywordFileLocation);
            } else {
                LOGGER.debug("Loading mysql keywords from classpath " + resourcePath);
                inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            }

            if (inputStream != null) {
                klist.addAll(loadKeywords(inputStream));
            } else {
                LOGGER.warn("Could not load mysql reserved keyword list from classpath!");
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return klist;
    }

}
