package com.virtusa.gto.nyql.configs

import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import java.nio.charset.StandardCharsets
/**
 * @author IWEERARATHNA
 */
class ConfigParser {

    private static final String BASE_CONF_KEY = '$baseConfiguration'
    private static final String REF_KEY = '$ref'

    static Map<String, Object> parseAndResolve(final File inputFile) {
        Map result = parse(inputFile)
        resolve(result, result) as Map
    }

    static Map<String, Object> parseAndResolve(final InputStream stream) {
        Map result = parse(stream)
        resolve(result, result) as Map
    }

    @CompileStatic
    static Map<String, Object> parse(InputStream inputConfigStream) {
        Map thisConf = directParse(inputConfigStream)
        String baseConfRef = (String) thisConf[BASE_CONF_KEY]
        if (baseConfRef == null || baseConfRef.isEmpty()) {
            return thisConf
        }

        thisConf.remove(BASE_CONF_KEY)

        InputStream baseRes = Thread.currentThread().contextClassLoader.getResourceAsStream(baseConfRef)
        if (baseRes != null) {
            try {
                Map baseConf = parse(baseRes)
                baseConf.putAll(thisConf)
                return baseConf
            } finally {
                baseRes.close()
            }
        }
        throw new NyConfigurationException('No base configuration is found in classpath location ' + baseConfRef + '!')
    }

    @CompileStatic
    static Map<String, Object> parse(final File inputFile) {
        File resolvedInputFile = inputFile.getCanonicalFile()
        Map thisConf = directParse(resolvedInputFile)
        if (!thisConf.containsKey(ConfigKeys.LOCATION_KEY)) {
            thisConf.put(ConfigKeys.LOCATION_KEY, resolvedInputFile.getParentFile().getAbsolutePath())
        }
        String baseConfRef = (String) thisConf[BASE_CONF_KEY]
        if (baseConfRef == null || baseConfRef.isEmpty()) {
            return thisConf
        }

        thisConf.remove(BASE_CONF_KEY)

        File baseFile = resolvedInputFile.getParentFile().toPath().resolve(baseConfRef).toFile()
        if (!baseFile.exists()) {
            throw new NyConfigurationException('No base configuration is found in location ' + baseFile.getAbsolutePath() + '!')
        }
        Map baseConf = parse(baseFile)
        baseConf.putAll(thisConf)
        baseConf
    }

    @CompileStatic
    private static Map<String, Object> directParse(InputStream inputStream) {
        new JsonSlurper().parse(inputStream, StandardCharsets.UTF_8.name()) as Map
    }

    @CompileStatic
    private static Map<String, Object> directParse(File inputFile) {
        new JsonSlurper().parse(inputFile, StandardCharsets.UTF_8.name()) as Map
    }

    @CompileStatic
    private static Object resolve(Map data, Object child) {
        if (child instanceof Map) {
            child.entrySet().each {
                resolve(data, it.value)
            }

            if (child.containsKey(REF_KEY)) {
                Object r = resolvePath(data, child[REF_KEY].toString())
                if (r instanceof Map) {
                    ((Map)child).remove(REF_KEY)
                    ((Map)child).putAll((Map)r)
                }
            }

        } else if (child instanceof Collection) {
            Collection collection = (Collection) child
            for (def item : collection) {
                resolve(data, item)
            }
        }

        child
    }

    private static Object resolvePath(Map data, String path) {
        String p = path
        if (p.startsWith('##')) {
            p = p.substring(2)
            String loc = data[ConfigKeys.LOCATION_KEY]
            if (loc != null) {
                File f = new File(new File(loc), p)
                return readPropertyContent(f)
            } else {
                return readPropertyContent(p)
            }

        } else if (p.startsWith('#')) {
            p = p.substring(1)
        }

        String[] parts = p.split('[/]')
        Map tmp = data
        for (int i = 0; i < parts.length; i++) {
            String k = parts[i]
            if (k.isEmpty()) {
                continue
            }
            if (tmp == null) {
                throw new NyConfigurationException('Cannot resolve ' + path + ' in input configuration json!')
            }
            tmp = tmp."$k"
        }
        tmp
    }

    @CompileStatic
    private static Map readPropertyContent(File file) {
        if (file.exists()) {
            return (Map) file.withInputStream {
                readPropertyContent(it)
            }
        }
        throw new NyConfigurationException("Configuration file does not exist in '${file.getAbsolutePath()}'!")
    }

    @CompileStatic
    private static Map readPropertyContent(String rPath) {
        InputStream baseRes = Thread.currentThread().contextClassLoader.getResourceAsStream(rPath)
        if (baseRes != null) {
            return readPropertyContent(baseRes)
        }
        throw new NyConfigurationException("Configuration file does not exist in resources '$rPath'!")
    }

    @CompileStatic
    private static Map readPropertyContent(InputStream inputStream) {
        Properties properties = new Properties()
        properties.load(inputStream)

        Map propSet = [:]
        propSet.putAll(properties)
        propSet
    }
}
