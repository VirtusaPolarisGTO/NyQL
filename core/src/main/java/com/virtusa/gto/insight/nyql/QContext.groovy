package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.db.QTranslator
import com.virtusa.gto.insight.nyql.model.QSession

/**
 * @author Isuru Weerarathna
 */
class QContext {

    QSession ownerSession
    QTranslator translator;
    String translatorName;

    Map<String, AParam> allParams= new HashMap<>()

    Tables tables = new Tables()
    Map<String, Column> columns = new HashMap<>()

    AParam addParam(AParam param) {
        if (allParams.containsKey(param.__name)) {
            return allParams[param.__name]
        }
        allParams.put(param.__name, param)
        return param
    }

    void renameColumn(String oldKey, String newKey, Column col) {
        Column oVal = columns.get(oldKey)
        columns.remove(oldKey)
        columns.put(newKey, oVal ?: col)
    }

    Column getColumnIfExist(String name) {
        return columns[name]
    }

    void mergeFrom(QContext otherCtx) {
        otherCtx.tables.each { k, t -> tables.putIfAbsent(k, t) }
        otherCtx.columns.each { k, c -> columns.putIfAbsent(k, c) }
        otherCtx.allParams.each {k, p -> allParams.putIfAbsent(k, p) }
    }
}
