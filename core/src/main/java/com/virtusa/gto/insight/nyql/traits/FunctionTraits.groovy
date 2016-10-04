package com.virtusa.gto.insight.nyql.traits

import com.virtusa.gto.insight.nyql.Column
import com.virtusa.gto.insight.nyql.FunctionColumn
import com.virtusa.gto.insight.nyql.utils.QUtils

/**
 * @author IWEERARATHNA
 */
trait FunctionTraits {

    def ADD(Object... cols)     {
        //List list = []
        //QUtils.expandToList(list, cols)
        return vColumn("op_add", cols)
    }
    def MINUS(Object op1, Object op2)     { return vColumn("op_minus", op1, op2) }
    def MULTIPLY(Object op1, Object op2)     { return vColumn("op_multiply", op1, op2) }
    def DIVIDE(Object op1, Object op2)     { return vColumn("op_divide", op1, op2) }
    def MODULUS(Object op1, Object op2)     { return vColumn("op_modulus", op1, op2) }
    def INVERSE(Object op1)     { return DIVIDE(1, op1) }


    def ASC(Column column)      { return fColumn(column, "asc") }
    def DESC(Column column)     { return fColumn(column, "desc") }
    def COUNT(Object column)    { return fColumn(column, "count") }
    def DISTINCT(Column column) { return fColumn(column, "distinct") }
    def MAX(Object column)      { return fColumn(column, "max") }
    def MIN(Object column)      { return fColumn(column, "min") }
    def AVG(Object column)      { return fColumn(column, "avg") }
    def SUM(Object column)      { return fColumn(column, "sum") }

    def LCASE(Column column)    { return fColumn(column, "lcase") }
    def UCASE(Column column)    { return fColumn(column, "ucase") }
    def TRIM(Column column)     { return fColumn(column, "trim") }
    def LEN(Column column)      { return fColumn(column, "len") }

    def ROUND(Column column, Object decimalPlaces)    { return vColumn("round", column, decimalPlaces) }
    def FLOOR(Column column) { return fColumn(column, "floor") }
    def CEIL(Column column) { return fColumn(column, "ceil") }
    def ABS(Column column) { return fColumn(column, "abs") }

    def CONCAT(Object... columns) { return vColumn("concat", columns) }

    /// Date time functions
    def NOW() { return fColumn(null, "current_timestamp") }
    def CURDATE() { return fColumn(null, "current_date") }
    def CURTIME() { return fColumn(null, "current_time") }
    def DATE_TRUNC(Column column) { return fColumn(column, "date_trunc") }
    def DATE_ADD(Object cDate, Object by, String intervalUnit) { return vColumn("date_add", cDate, by, intervalUnit) }
    def DATE_SUB(Object cDate, Object by, String intervalUnit) { return vColumn("date_sub", cDate, by, intervalUnit) }

    private FunctionColumn vColumn(String fName, Object... columns) {
        List<Object> vals = new ArrayList<>()
        vals.addAll(columns)
        return new FunctionColumn(_columns: vals, _func: fName, _setOfCols: true, _ctx: _ctx)
    }

    private FunctionColumn fColumn(Object column, String fName) {
        return new FunctionColumn(_wrapper: column, _func: fName, _ctx: _ctx)
    }
}