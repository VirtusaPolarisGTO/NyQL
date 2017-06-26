package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.QResultProxy
import com.virtusa.gto.nyql.exceptions.NyException

/**
 * Base interface to implementation for a repository which responsible
 * for parsing scripts.
 *
 * @author IWEERARATHNA
 */
trait QRepository implements Closeable {

    /**
     * Clears the cache.
     *
     * @param level which level to clear.
     */
    abstract void clearCache(int level)

    /**
     * Reload the script for the given id.
     *
     * @param id unique script id.
     */
    abstract void reloadScript(String id) throws NyException

    /**
     * Parse the given script represented by given id using the session instance provided.
     *
     * @param scriptId script id to parse.
     * @param session session instance to use for parsing.
     * @return parsed script instance.
     * @throws com.virtusa.gto.nyql.exceptions.NyException any exception thrown while parsing.
     */
    abstract QScript parse(String scriptId, QSession session) throws NyException

    /**
     * Parses an already given generated query and wraps it into a script.
     *
     * @param resultProxy already generated query.
     * @param session session instance.
     * @return generated script wrapped instance.
     * @throws NyException any exception thrown while parsing.
     */
    QScript parse(QResultProxy resultProxy, QSession session = null) throws NyException {
        new QScript(id: session?.currentActiveScript(), proxy: resultProxy, qSession: session)
    }

}