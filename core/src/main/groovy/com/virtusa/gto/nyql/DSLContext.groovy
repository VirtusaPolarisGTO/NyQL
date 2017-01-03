package com.virtusa.gto.nyql

import com.virtusa.gto.nyql.db.QDbFactory

/**
 * @author IWEERARATHNA
 */
class DSLContext {

    String activeDb
    QDbFactory activeFactory

    DSLContext(String dbName) {
        activeDb = dbName
    }

}
