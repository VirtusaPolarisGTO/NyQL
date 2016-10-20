package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.db.QTranslator
import com.virtusa.gto.insight.nyql.model.QSession
import com.virtusa.gto.insight.nyql.model.units.AParam

/**
 * @author Isuru Weerarathna
 */
class QContext {

    QSession ownerSession
    QTranslator translator;
    String translatorName;

    //Map<String, AParam> allParams = new HashMap<>()
    List<AParam> allParams = [] as Queue

    Tables tables = new Tables()
    Map<String, Column> columns = new HashMap<>()
    Query ownQuery

    QContext cloneContext() {
        QContext qContext = new QContext()
        qContext.tables.putAll(this.tables)
        qContext.columns.putAll(this.columns)
        qContext.allParams.addAll(this.allParams)
        qContext
    }

    Table getTheOnlyTable() {
        if (tables.size() != 1) {
            return null;
        }
        for (String k : tables.keySet()) {
            return tables[k]
        }
        return null;
    }

    AParam addParam(AParam param) {
        //if (allParams.containsKey(param.__name)) {
        //    return allParams[param.__name]
        //}
        //allParams.put(param.__name, param)
        allParams.add(param)
        param
    }

    void renameColumn(String oldKey, String newKey, Column col) {
        Column oVal = columns.get(oldKey)
        columns.remove(oldKey)
        columns.put(newKey, oVal ?: col)
    }

    Column getColumnIfExist(String name) {
        columns[name]
    }

    void mergeFrom(QContext otherCtx) {
        otherCtx.tables.each { k, t -> tables.putIfAbsent(k, t) }
        otherCtx.columns.each { k, c -> columns.put(k, c) }
        otherCtx.allParams.each { p -> allParams.add(p) }
    }
}
