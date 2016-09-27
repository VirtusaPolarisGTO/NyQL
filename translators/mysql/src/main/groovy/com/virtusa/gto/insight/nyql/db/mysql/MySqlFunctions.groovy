package com.virtusa.gto.insight.nyql.db.mysql

import com.virtusa.gto.insight.nyql.Column
import com.virtusa.gto.insight.nyql.FunctionColumn
import com.virtusa.gto.insight.nyql.QContextType
import com.virtusa.gto.insight.nyql.db.QFunctions
import com.virtusa.gto.insight.nyql.exceptions.NyException

/**
 * @author Isuru Weerarathna
 */
trait MySqlFunctions implements QFunctions {

    def from_unixtime(it) {
            if (it instanceof String)
                return "FROM_UNIXTIME($it)"
            else
                return "FROM_UNIXTIME(" + ___resolve(it, QContextType.SELECT) + ")"
    }
    def unix_timestamp(it) {
            if (it == null) {
                return "UNIX_TIMESTAMP()"
            } else if (it instanceof String)
                return "UNIX_TIMESTAMP($it)"
            else
                return "UNIX_TIMESTAMP(" + ___resolve(it, QContextType.SELECT) + ")"
    }

    String mysql_cast(it) {
            if (it instanceof List)
                return "CAST(" + ___resolve(it[0], QContextType.SELECT) + " AS " + ___resolve(it[1], QContextType.SELECT) + ")"
            else
                throw new NyException("CAST function expects two parameters!")
    }

    def FROM_EPOCH(Column column)     { return fColumn(column, "from_unixtime") }
    def EPOCH_TIMESTAMP(Column column=null) { return fColumn(column, "unix_timestamp") }
    def EPOCH_DATE(Column column) { return CAST(FROM_EPOCH(column), "Date") }

    def CAST(Column source, Object toType) {
        return vColumn("mysql_cast", source, toType)
    }

    private FunctionColumn vColumn(String fName, Object... columns) {
        List<Object> vals = new ArrayList<>()
        vals.addAll(columns)
        return new FunctionColumn(_columns: vals, _func: fName, _setOfCols: true, _ctx: _ctx)
    }

    private FunctionColumn fColumn(Column column, String fName) {
        return new FunctionColumn(_wrapper: column, _func: fName, _ctx: _ctx)
    }

}