package com.virtusa.gto.insight.nyql.configs

import com.virtusa.gto.insight.nyql.DSLContext
import com.virtusa.gto.insight.nyql.db.QDbFactory
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.QDatabaseRegistry
import org.apache.commons.lang3.BooleanUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author IWEERARATHNA
 */
class Configurations {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configurations.class)

    private Properties properties

    private Configurations() {}

    Configurations configFrom(File propertyFile) throws NyException {
        Properties temp = new Properties()

        propertyFile.withInputStream {
            temp.load(it)
        }
        return configure(temp)
    }

    Configurations configure(Properties configProps) throws NyException {
        properties = configProps

        doConfig()
        return this
    }

    private void doConfig() throws NyException {
        def clzNames = getAvailableTranslators()
        if (clzNames != null && clzNames.length > 0) {
            clzNames.each {
                try {
                    def factory = Class.forName(it).newInstance() as QDbFactory
                    QDatabaseRegistry.instance.register(factory)
                } catch (ClassNotFoundException ex) {
                    throw new NyException("No database implementation class found by name '$it'!", ex)
                }
            }
        }

        String activeDb = getActivatedDb()
        if (activeDb != null) {
            LOGGER.debug("Activating DB: {}", activeDb)
            QDatabaseRegistry.instance.load(activeDb)
        } else {
            throw new NyException("No database has been activated!")
        }
    }

    String getActivatedDb() {
        return properties?.getProperty("activate")
    }

    boolean cacheRawScripts() {
        return toBool(properties?.getProperty("cache.raw.scripts"), true)
    }

    boolean cacheGeneratedQueries() {
        return toBool(properties?.getProperty("cache.queries"), true)
    }

    String[] getAvailableTranslators() {
        return properties?.getProperty("translators")?.toString()?.split("[,]")
    }

    private static boolean toBool(Object value, boolean defValue) {
        if (value == null) {
            return defValue
        } else {
            return BooleanUtils.toBoolean(String.valueOf(value))
        }
    }

    static Configurations instance() {
        return Holder.INSTANCE
    }

    private static class Holder {
        private static final Configurations INSTANCE = new Configurations()
    }

}
