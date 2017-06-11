package com.virtusa.gto.nyql.engine.impl

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.engine.exceptions.NyScriptExecutionException
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.utils.QUtils
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * @author IWEERARATHNA
 */
@CompileStatic
@PackageScope
class JdbcHelperUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcHelperUtils)

    /**
     * Given parameter value will be converted into jdbc compatible timestamp.
     *
     * @param value parameter value.
     * @param configurations nyql configuration instance to be used when parsing.
     * @param tsFormat timestamp custom format.
     * @return new timestamp instance corresponding to the given value.
     */
    @SuppressWarnings('InstanceOf')
    static Timestamp convertTimestamp(Object value, Configurations configurations, String tsFormat = null) {
        if (value instanceof Long) {
            Timestamp.from(Instant.ofEpochMilli((long)value))
        } else {
            if (tsFormat == null) {
                Timestamp.from(Instant.from(configurations.timestampFormatter.parse(String.valueOf(value))))
            } else {
                Timestamp.from(Instant.from(DateTimeFormatter.ofPattern(tsFormat).parse(String.valueOf(value))))
            }
        }
    }

    /**
     * Given parameter value will be converted into jdbc compatible date instance.
     *
     * @param value parameter value.
     * @return new date instance corresponding to the given value.
     */
    static Date convertToDate(String value) {
        Date.valueOf(value)
    }

    /**
     * Converts given parameter values to a binary input stream so that it can be assigned in
     * prepared statement easily.
     *
     * @param value input parameter value.
     * @return an input stream for prepared statement.
     */
    static InputStream convertBinary(Object value) {
        if (value instanceof byte[]) {
            new ByteArrayInputStream((byte[])value)
        } else if (value instanceof InputStream) {
            (InputStream)value
        } else {
            // assumes string and in base64 format
            new ByteArrayInputStream(Base64.decoder.decode(String.valueOf(value)));
        }
    }

    @CompileStatic
    static void logScript(QScript script, int logLevel) throws NyScriptExecutionException {
        if (script.proxy.query == null) {
            throw new NyScriptExecutionException(QUtils.generateErrStr(
                    'Generated query for execution is empty! [SCRIPT: ' + script.id + ']',
                    'Did you accidentally set cache true to this script?',
                    'Did you happen to send incorrect data variables to the script?'))
        }

        String q = "Query @ ${script.id}: -----------------------------------------------------\n" +
                script.proxy.query.trim()
        String qs = '------------------------------------------------------------'
        if (1 == logLevel && LOGGER.isTraceEnabled()) {
            LOGGER.trace(q)
            LOGGER.trace(qs)
        } else if (logLevel == 2 && LOGGER.isDebugEnabled()) {
            LOGGER.debug(q)
            LOGGER.debug(qs)
        } else if (logLevel == 3 && LOGGER.isInfoEnabled()) {
            LOGGER.info(q)
            LOGGER.info(qs)
        } else if (logLevel == 4 && LOGGER.isWarnEnabled()) {
            LOGGER.warn(q)
            LOGGER.warn(qs)
        } else if (logLevel == 5 && LOGGER.isErrorEnabled()) {
            LOGGER.error(q)
            LOGGER.error(qs)
        }
    }

    @CompileStatic
    static void logParameter(Object pcount, Object itemValue, int logLevel) {
        String q = ' Parameter #' + pcount + ' : ' + itemValue + ' [' + (itemValue != null ? itemValue.class.simpleName : '') + ']'
        if (logLevel == 1 && LOGGER.isTraceEnabled()) {
            LOGGER.trace(q)
        } else if (logLevel == 2 && LOGGER.isDebugEnabled()) {
            LOGGER.debug(q)
        } else if (logLevel == 3 && LOGGER.isDebugEnabled()) {
            LOGGER.info(q)
        } else if (logLevel == 4 && LOGGER.isDebugEnabled()) {
            LOGGER.warn(q)
        } else if (logLevel == 5 && LOGGER.isDebugEnabled()) {
            LOGGER.error(q)
        }
    }
}
