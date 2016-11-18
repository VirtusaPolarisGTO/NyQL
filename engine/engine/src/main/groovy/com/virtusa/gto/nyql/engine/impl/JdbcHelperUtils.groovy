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

    static Date convertToDate(String value) {
        Date.valueOf(value)
    }

}
