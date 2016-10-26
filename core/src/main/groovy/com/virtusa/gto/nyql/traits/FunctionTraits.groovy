package com.virtusa.gto.nyql.traits

import com.virtusa.gto.nyql.Column
import com.virtusa.gto.nyql.FunctionColumn
import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
trait FunctionTraits {

    @CompileStatic FunctionColumn ADD(Object... cols)     { vColumn('op_add', cols) }
    @CompileStatic FunctionColumn MINUS(Object op1, Object op2)     { vColumn('op_minus', op1, op2) }
    @CompileStatic FunctionColumn MULTIPLY(Object op1, Object op2)     { vColumn('op_multiply', op1, op2) }
    @CompileStatic FunctionColumn DIVIDE(Object op1, Object op2)     { vColumn('op_divide', op1, op2) }
    @CompileStatic FunctionColumn MODULUS(Object op1, Object op2)     { vColumn('op_modulus', op1, op2) }
    @CompileStatic FunctionColumn INVERSE(Object op1)     { DIVIDE(1, op1) }

    @CompileStatic FunctionColumn BITAND(Object left, Object right) { vColumn('op_bit_and', left, right) }
    @CompileStatic FunctionColumn BITOR(Object left, Object right) { vColumn('op_bit_or', left, right) }
    @CompileStatic FunctionColumn BITXOR(Object left, Object right) { vColumn('op_bit_xor', left, right) }
    @CompileStatic FunctionColumn BITNOT(Object left) { fColumn(left, 'op_bit_not') }

    @CompileStatic FunctionColumn ASC(Column column)      { fColumn(column, 'asc') }
    @CompileStatic FunctionColumn DESC(Column column)     { fColumn(column, 'desc') }
    @CompileStatic FunctionColumn COUNT(Object column)    { fColumn(column, 'count') }
    @CompileStatic FunctionColumn DISTINCT(Column column) { fColumn(column, 'distinct') }
    @CompileStatic FunctionColumn MAX(Object column)      { fColumn(column, 'max') }
    @CompileStatic FunctionColumn MIN(Object column)      { fColumn(column, 'min') }
    @CompileStatic FunctionColumn AVG(Object column)      { fColumn(column, 'avg') }
    @CompileStatic FunctionColumn SUM(Object column)      { fColumn(column, 'sum') }

    @CompileStatic FunctionColumn LCASE(Column column)    { fColumn(column, 'lcase') }
    @CompileStatic FunctionColumn UCASE(Column column)    { fColumn(column, 'ucase') }
    @CompileStatic FunctionColumn TRIM(Column column)     { fColumn(column, 'trim') }
    @CompileStatic FunctionColumn LEN(Column column)      { fColumn(column, 'len') }
    @CompileStatic FunctionColumn SUBSTRING(Object column, Object start, Object length) { vColumn('substr', column, start, length) }
    @CompileStatic FunctionColumn SUBSTRING(Object column, Object start) { vColumn('substr', column, start) }
    @CompileStatic FunctionColumn POSITION(Object column, Object substr) { vColumn('position', column, substr) }

    @CompileStatic FunctionColumn ROUND(Column column, Object decimalPlaces)    { vColumn('round', column, decimalPlaces) }
    @CompileStatic FunctionColumn FLOOR(Column column) { fColumn(column, 'floor') }
    @CompileStatic FunctionColumn CEIL(Column column) { fColumn(column, 'ceil') }
    @CompileStatic FunctionColumn ABS(Column column) { fColumn(column, 'abs') }

    @CompileStatic FunctionColumn CONCAT(Object... columns) { vColumn("concat", columns) }

    // cast function
    @CompileStatic FunctionColumn CAST_INT(Object column) { fColumn(column, 'cast_to_int') }
    @CompileStatic FunctionColumn CAST_STR(Object column) { fColumn(column, 'cast_to_str') }
    @CompileStatic FunctionColumn CAST_DATE(Object column) { fColumn(column, 'cast_to_date') }

    /// Date time functions
    @CompileStatic FunctionColumn NOW() { fColumn(null, 'current_timestamp') }
    @CompileStatic FunctionColumn CURDATE() { fColumn(null, 'current_date') }
    @CompileStatic FunctionColumn CURTIME() { fColumn(null, 'current_time') }
    @CompileStatic FunctionColumn CUREPOCH() { fColumn(null, 'current_epoch') }
    @CompileStatic FunctionColumn DATE_TRUNC(Column column) { fColumn(column, 'date_trunc') }

    @CompileStatic FunctionColumn EPOCH_TO_DATE(Column column)     { fColumn(column, 'epoch_to_date') }
    @CompileStatic FunctionColumn EPOCH_TO_DATETIME(Column column)     { fColumn(column, 'epoch_to_datetime') }

    @CompileStatic FunctionColumn DATE_DIFF_YEARS(Object sDate, Object eDate) { vColumn('date_diff_years', sDate, eDate) }
    @CompileStatic FunctionColumn DATE_DIFF_MONTHS(Object sDate, Object eDate) { vColumn('date_diff_months', sDate, eDate) }
    @CompileStatic FunctionColumn DATE_DIFF_DAYS(Object sDate, Object eDate) { vColumn('date_diff_days', sDate, eDate) }
    @CompileStatic FunctionColumn DATE_DIFF_WEEKS(Object sDate, Object eDate) { vColumn('date_diff_weeks', sDate, eDate) }
    @CompileStatic FunctionColumn DATE_DIFF_HOURS(Object sDate, Object eDate) { vColumn('date_diff_hours', sDate, eDate) }
    @CompileStatic FunctionColumn DATE_DIFF_MINUTES(Object sDate, Object eDate) { vColumn('date_diff_minutes', sDate, eDate) }
    @CompileStatic FunctionColumn DATE_DIFF_SECONDS(Object sDate, Object eDate) { vColumn('date_diff_seconds', sDate, eDate) }
    @CompileStatic FunctionColumn DATE_ADD_DAYS(Object cDate, Object by) { vColumn('date_add_days', cDate, by) }
    @CompileStatic FunctionColumn DATE_ADD_MONTHS(Object cDate, Object by) { vColumn('date_add_months', cDate, by) }
    @CompileStatic FunctionColumn DATE_ADD_YEARS(Object cDate, Object by) { vColumn('date_add_years', cDate, by) }
    @CompileStatic FunctionColumn DATE_ADD_WEEKS(Object cDate, Object by) { vColumn('date_add_weeks', cDate, by) }
    @CompileStatic FunctionColumn DATE_ADD_HOURS(Object cDate, Object by) { vColumn('date_add_hours', cDate, by) }
    @CompileStatic FunctionColumn DATE_ADD_MINUTES(Object cDate, Object by) { vColumn('date_add_minutes', cDate, by) }
    @CompileStatic FunctionColumn DATE_ADD_SECONDS(Object cDate, Object by) { vColumn('date_add_seconds', cDate, by) }
    @CompileStatic FunctionColumn DATE_SUB_DAYS(Object cDate, Object by) { vColumn('date_sub_days', cDate, by) }
    @CompileStatic FunctionColumn DATE_SUB_MONTHS(Object cDate, Object by) { vColumn('date_sub_months', cDate, by) }
    @CompileStatic FunctionColumn DATE_SUB_YEARS(Object cDate, Object by) { vColumn('date_sub_years', cDate, by) }
    @CompileStatic FunctionColumn DATE_SUB_WEEKS(Object cDate, Object by) { vColumn('date_sub_weeks', cDate, by) }
    @CompileStatic FunctionColumn DATE_SUB_HOURS(Object cDate, Object by) { vColumn('date_sub_hours', cDate, by) }
    @CompileStatic FunctionColumn DATE_SUB_MINUTES(Object cDate, Object by) { vColumn('date_sub_minutes', cDate, by) }
    @CompileStatic FunctionColumn DATE_SUB_SECONDS(Object cDate, Object by) { vColumn('date_sub_seconds', cDate, by) }

    private FunctionColumn vColumn(String fName, Object... columns) {
        List<Object> vals = new ArrayList<>()
        vals.addAll(columns)
        new FunctionColumn(_columns: vals, _func: fName, _setOfCols: true, _ctx: _ctx)
    }

    private FunctionColumn fColumn(Object column, String fName) {
        new FunctionColumn(_wrapper: column, _func: fName, _ctx: _ctx)
    }
}