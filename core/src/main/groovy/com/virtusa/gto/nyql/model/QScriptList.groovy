package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.Query
import groovy.transform.ToString
/**
 * @author IWEERARATHNA
 */
@ToString(includes = ['scripts'])
class QScriptList extends QScript {

    List<QScript> scripts
    QScriptListType type = QScriptListType.DEFAULT
    Query baseQuery

    @Override
    QScript spawn(QSession session) {
        QScriptList scriptList = new QScriptList(id: id, qSession: session)
        if (scripts != null) {
            for (QScript script : scripts) {
                scriptList.scripts.add(script.spawn(session))
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
    QScript spawn() {
        spawn(null)
    }

    @Override
    void free() {
        super.free()
        if (scripts != null) {
            scripts.each { it.free() }
            scripts.clear()
        }
    }

}
