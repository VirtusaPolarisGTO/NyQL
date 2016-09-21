package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.db.QDbFactory
import com.virtusa.gto.insight.nyql.db.QTranslator
import com.virtusa.gto.insight.nyql.model.QDatabaseRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
