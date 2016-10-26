package com.virtusa.gto.nyql

import com.virtusa.gto.nyql.db.QDbFactory

/**
 * @author IWEERARATHNA
 */
class DSLContext {

    static DSLContext activeDSLContext;

    String activeDb
    QDbFactory activeFactory

    DSLContext(String dbName) {
        activeDb = dbName
    }

    static DSLContext register(String dbName) {
        DSLContext dslContext = new DSLContext(dbName)
        activeDSLContext = dslContext
        return dslContext
    }

}
