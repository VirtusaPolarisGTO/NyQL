package com.virtusa.gto.insight.nyql.engine.repo

import com.virtusa.gto.insight.nyql.configs.Configurations
import com.virtusa.gto.insight.nyql.model.QScript

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

/**
 * @author IWEERARATHNA
 */
class Caching {

    private final Map<String, QScript> cache = new ConcurrentHashMap<>()

    private final Map<String, Script> compiledCache = new ConcurrentHashMap<>()

    boolean hasGeneratedQuery(String scriptId) {
        return cache.containsKey(scriptId)
    }

    QScript getGeneratedQuery(String scriptId) {
        return cache.get(scriptId)
    }

    QScript addGeneratedQuery(String scriptId, QScript script) {
        cache.put(scriptId, script)
        return script
    }

    Script getCompiledScript(String scriptId) {
        return compiledCache.get(scriptId)
    }

    Script compileIfAbsent(String scriptId, Function<? extends String, ? extends Script> generator) {
        if (Configurations.instance().cacheRawScripts()) {
            return compiledCache.computeIfAbsent(scriptId, generator)
        } else {
            return generator.apply(scriptId);
        }
    }

    void clearGeneratedCache() {
        cache.clear()
    }

    void invalidateGeneratedCache(String scriptId) {
        cache.remove(scriptId)
    }
}
