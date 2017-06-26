package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.exceptions.NyScriptNotFoundException

import java.util.function.Function

/**
 * Interface responsible of providing script contents to a repository.
 *
 * @author IWEERARATHNA
 */
trait QScriptMapper implements Function<String, QSource> {

    @Override
    QSource apply(String s) {
        map(s)
    }

    /**
     * Returns a script source instance for the given id.
     *
     * @param id script id.
     * @return corresponding source for the given id.
     */
    abstract QSource map(String id) throws NyScriptNotFoundException

    /**
     * Returns all sources available from this mapper.
     * This will be used to cache scripts in advance/startup.
     *
     * @return list of all sources in this mapper.
     */
    abstract Collection<QSource> allSources()

    /**
     * Returns whether this mapper sources can be cached in advance.
     *
     * @return possibility of caching.
     */
    abstract boolean canCacheAtStartup()

    /**
     * Reloads the script by given id.
     *
     * @param id unique script id.
     * @return reloaded script.
     * @throws NyScriptNotFoundException when no script is found for given id.
     */
    abstract QSource reload(String id) throws NyScriptNotFoundException
}
