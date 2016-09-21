package com.virtusa.gto.insight.nyql.engine.impl

import groovy.transform.PackageScope

import java.sql.JDBCType

/**
 * @author IWEERARATHNA
 */
@PackageScope class TypeIdentifiers {

    static Map<Class<?>, JDBCType> jdbcTypeMap = [:]
    static Map<JDBCType, JdbcConsumer> typeAssignerMap = [:]

    static {
        jdbcTypeMap.put(int.class, JDBCType.INTEGER)
        jdbcTypeMap.put(short.class, JDBCType.SMALLINT)
        jdbcTypeMap.put(byte.class, JDBCType.TINYINT)
        jdbcTypeMap.put(boolean.class, JDBCType.BOOLEAN)
        jdbcTypeMap.put(double.class, JDBCType.DOUBLE)
        jdbcTypeMap.put(float.class, JDBCType.FLOAT)
        jdbcTypeMap.put(long.class, JDBCType.BIGINT)
        jdbcTypeMap.put(BigDecimal.class, JDBCType.NUMERIC)
        jdbcTypeMap.put(String.class, JDBCType.VARCHAR)

        typeAssignerMap.put(JDBCType.INTEGER, { stmt, idx, val -> stmt.setInt(idx, (int)val) })
        typeAssignerMap.put(JDBCType.BOOLEAN, { stmt, idx, val -> stmt.setBoolean(idx, (boolean)val) })
        typeAssignerMap.put(JDBCType.TINYINT, { stmt, idx, val -> stmt.setByte(idx, (byte)val) })
        typeAssignerMap.put(JDBCType.SMALLINT, { stmt, idx, val -> stmt.setShort(idx, (short)val) })
        typeAssignerMap.put(JDBCType.DOUBLE, { stmt, idx, val -> stmt.setDouble(idx, (double)val) })
        typeAssignerMap.put(JDBCType.FLOAT, { stmt, idx, val -> stmt.setFloat(idx, (float)val) })
        typeAssignerMap.put(JDBCType.BIGINT, { stmt, idx, val -> stmt.setLong(idx, (long)val) })
        typeAssignerMap.put(JDBCType.NUMERIC, { stmt, idx, val -> stmt.setBigDecimal(idx, (BigDecimal)val) })
        typeAssignerMap.put(JDBCType.VARCHAR, { stmt, idx, val -> stmt.setString(idx, String.valueOf(val)) })
        typeAssignerMap.put(JDBCType.NVARCHAR, { stmt, idx, val -> stmt.setNString(idx, String.valueOf(val)) })
        typeAssignerMap.put(JDBCType.NVARCHAR, { stmt, idx, val -> stmt.set(idx, String.valueOf(val)) })
    }

}
