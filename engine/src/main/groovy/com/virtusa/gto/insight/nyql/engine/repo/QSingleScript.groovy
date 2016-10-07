package com.virtusa.gto.insight.nyql.engine.repo

import com.virtusa.gto.insight.nyql.configs.ConfigBuilder
import com.virtusa.gto.insight.nyql.model.QScriptMapper
import com.virtusa.gto.insight.nyql.model.QSource

/**
 * @author IWEERARATHNA
 */
class QSingleScript implements QScriptMapper {

    private final GroovyCodeSource codeSource
    private final QSource qSource
    private final List<QSource> allSources

    QSingleScript(String id, String content) {
        codeSource = new GroovyCodeSource(content, id, GroovyShell.DEFAULT_CODE_BASE)
        qSource = new QSource(id: id, file: null, doCache: false, codeSource: codeSource)
        allSources = Arrays.asList(qSource)
    }

    @Override
    QSource map(String id) {
        return qSource
    }

    @Override
    Collection<QSource> allSources() {
        return allSources
    }

    @Override
    boolean canCacheAtStartup() {
        return true
    }
}
