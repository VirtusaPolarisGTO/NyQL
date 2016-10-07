package com.virtusa.gto.insight.nyql.model

import java.util.function.Function

/**
 * @author IWEERARATHNA
 */
trait QScriptMapper implements Function<String, QSource> {

    @Override
    QSource apply(String s) {
        return map(s)
    }

    abstract QSource map(String id)

    abstract Collection<QSource> allSources()

    abstract boolean canCacheAtStartup()

}
