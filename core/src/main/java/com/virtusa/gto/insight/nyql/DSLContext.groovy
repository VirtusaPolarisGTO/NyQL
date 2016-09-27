package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.db.QTranslator

/**
 * @author IWEERARATHNA
 */
class DSLContext {

    static DSLContext activeDSLContext;

    String activeDb
    QTranslator qTranslator
    List<Class<?>> dbTraits

    DSLContext(String dbName) {
        activeDb = dbName
    }

    static DSLContext register(String dbName) {
        DSLContext dslContext = new DSLContext(dbName)
        activeDSLContext = dslContext
        return dslContext
    }

}
