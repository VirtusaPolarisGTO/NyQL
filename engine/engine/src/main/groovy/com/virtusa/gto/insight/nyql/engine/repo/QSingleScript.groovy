package com.virtusa.gto.insight.nyql.engine.repo

import com.virtusa.gto.insight.nyql.model.QScriptMapper
import com.virtusa.gto.insight.nyql.model.QSource
import groovy.transform.CompileStatic

/**
 * Contains a mapper for a single folder.
 *
 * @author IWEERARATHNA
 */
@CompileStatic
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
        qSource
    }

    @Override
    Collection<QSource> allSources() {
        allSources
    }

    @Override
    boolean canCacheAtStartup() {
        true
    }
}
