package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import groovy.transform.CompileStatic

import java.sql.Connection

/**
 * @author iweerarathna
 */
@CompileStatic
class DbInfo {

    public static final DbInfo UNRESOLVED = new DbInfo()

    int majorVersion
    int minorVersion
    String vendor

    static DbInfo deriveFromConnection(Connection connection) throws Exception {
        if (connection == null) {
            return UNRESOLVED
        }

        DbInfo dbInfo = new DbInfo()
        try {
            dbInfo.setMajorVersion(connection.getMetaData().databaseMajorVersion)
            dbInfo.setMinorVersion(connection.getMetaData().databaseMinorVersion)
            dbInfo.setVendor(connection.getMetaData().databaseProductName)
        } catch (Exception ex) {
            throw new NyConfigurationException("Error occurred while retrieving database information!", ex)
        } finally {
            if (connection != null) {
                connection.close()
            }
        }
        dbInfo
    }

}
