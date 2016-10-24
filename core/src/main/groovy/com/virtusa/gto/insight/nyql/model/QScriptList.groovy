package com.virtusa.gto.insight.nyql.model

import groovy.transform.ToString

import java.util.stream.Collectors

/**
 * @author IWEERARATHNA
 */
@ToString(includes = ["scripts"])
class QScriptList extends QScript {

    List<QScript> scripts

    @Override
    QScript spawn() {
        QScriptList scriptList = new QScriptList(id: id, qSession: (QSession)null)
        if (scripts != null) {
            for (QScript script : scripts) {
                scriptList.scripts.add(script.spawn())
            }
        }
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
        return "QScriptList{" +
                "scripts=\n" + scripts.stream().map({it.toString()}).collect(Collectors.joining(",\n")) +
                '}';
    }
}
