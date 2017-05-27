package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.Query
import groovy.transform.ToString

import java.util.stream.Collectors
/**
 * @author IWEERARATHNA
 */
@ToString(includes = ['scripts'])
class QScriptList extends QScript {

    List<QScript> scripts
    QScriptListType type = QScriptListType.DEFAULT
    Query baseQuery

    @Override
    QScript spawn() {
        QScriptList scriptList = new QScriptList(id: id, qSession: (QSession)null)
        if (scripts != null) {
            for (QScript script : scripts) {
                scriptList.scripts.add(script.spawn())
            }
        }
        if (baseQuery != null) {
            baseQuery._ctx = null
            scriptList.baseQuery = baseQuery
        }
        scriptList.type = type
        scriptList
    }

    @Override
    void free() {
        super.free()
        if (scripts != null) {
            scripts.each { it.free() }
            scripts.clear()
        }
    }

    @Override
    public String toString() {
        'QScriptList{' +
                'scripts=\n' + scripts.stream().map {it.toString()}.collect(Collectors.joining(',\n')) +
                '}';
    }
}
