package com.virtusa.gto.nyql.engine.impl

import com.virtusa.gto.nyql.configs.Configurations
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

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
}
