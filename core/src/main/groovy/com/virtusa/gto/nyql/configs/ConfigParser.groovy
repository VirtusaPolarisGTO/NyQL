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

    public static Map parseAndResolve(final File inputFile) {
        Map result = parse(inputFile)
        resolve(result, result) as Map
    }

    @CompileStatic
    public static Map parse(final File inputFile) {
        File resolvedInputFile = inputFile.getCanonicalFile()
        Map thisConf = new JsonSlurper().parse(resolvedInputFile, StandardCharsets.UTF_8.name()) as Map
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
        if (p.startsWith('#')) {
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
}
