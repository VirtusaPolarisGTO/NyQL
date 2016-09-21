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

    private static final Logger LOGGER = LoggerFactory.getLogger(DSLContext.class)

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

    static def load(Map options) {
        def clzNames = options.get("translators")?.toString()?.split("[,]")
        if (clzNames != null && clzNames.length > 0) {
            clzNames.each {
                def factory = Class.forName(it).newInstance() as QDbFactory

                QDatabaseRegistry.instance.register(factory)
            }
        }

        String activeDb = options["activate"]
        if (activeDb != null) {
            LOGGER.debug("Activating DB: {}", activeDb)
            return QDatabaseRegistry.instance.load(activeDb)
        }
        return null
    }

}
