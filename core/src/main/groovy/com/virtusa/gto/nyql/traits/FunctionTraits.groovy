package com.virtusa.gto.nyql.traits

import com.virtusa.gto.nyql.Column
import com.virtusa.gto.nyql.FunctionColumn
import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
trait FunctionTraits {

    @CompileStatic FunctionColumn COALESCE(Object... cols) { vColumn("coalesce", cols) }

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

    @CompileStatic FunctionColumn ASC(Object column)      { fColumn(column, 'asc') }
    @CompileStatic FunctionColumn DESC(Object column)     { fColumn(column, 'desc') }
    @CompileStatic FunctionColumn COUNT(Object column)    { fColumn(column, 'count') }
    @CompileStatic FunctionColumn DISTINCT(Column column) { fColumn(column, 'distinct') }
    @CompileStatic FunctionColumn MAX(Object column)      { fColumn(column, 'max') }
    @CompileStatic FunctionColumn MIN(Object column)      { fColumn(column, 'min') }
    @CompileStatic FunctionColumn AVG(Object column)      { fColumn(column, 'avg') }
    @CompileStatic FunctionColumn SUM(Object column)      { fColumn(column, 'sum') }

    // stats functions
    @CompileStatic FunctionColumn STDDEV_POP(Object expr) { fColumn(expr, 'stat_stddevpop') }
    @CompileStatic FunctionColumn STDDEV_SAMP(Object expr) { fColumn(expr, 'stat_stddevsamp') }
    @CompileStatic FunctionColumn VAR_POP(Object expr) { fColumn(expr, 'stat_varpop') }
    @CompileStatic FunctionColumn VAR_SAMP(Object expr) { fColumn(expr, 'stat_varsamp') }

    /*
        String Functions
     */

    @CompileStatic FunctionColumn CONCAT(Object... columns) { vColumn('concat', columns) }
    @CompileStatic FunctionColumn CONCAT_WS(Object separator, Object... columns) { vColumn('concat_ws', separator, columns) }
    @CompileStatic FunctionColumn CONCAT_NN(Object... columns) { vColumn('concat_nn', columns) }
    @CompileStatic FunctionColumn LCASE(Object column)    { fColumn(column, 'lcase') }
    @CompileStatic FunctionColumn LEFT_PAD(Object column, Object length) { vColumn('str_lpad', column, length) }
    @CompileStatic FunctionColumn LEFT_PAD(Object column, Object length, Object text) { vColumn('str_lpad', column, length, text) }
    @CompileStatic FunctionColumn LEFT_TRIM(Object column) { fColumn(column, 'str_ltrim') }
    @CompileStatic FunctionColumn LEN(Object column)      { fColumn(column, 'len') }
    @CompileStatic FunctionColumn POSITION(Object column, Object substr) { vColumn('position', column, substr) }
    @CompileStatic FunctionColumn POSITION_LAST(Object column, Object substr) { vColumn('position_last', column, substr) }
    @CompileStatic FunctionColumn REPEAT(Object str, Object noOfTimes) { vColumn('str_repeat', str, noOfTimes) }
    @CompileStatic FunctionColumn REVERSE(Object column) { fColumn(column, 'str_reverse') }
    @CompileStatic FunctionColumn RIGHT_PAD(Object column, Object length) { vColumn('str_rpad', column, length) }
    @CompileStatic FunctionColumn RIGHT_PAD(Object column, Object length, Object text) { vColumn('str_rpad', column, length, text) }
    @CompileStatic FunctionColumn RIGHT_TRIM(Object column) { fColumn(column, 'str_rtrim') }
    @CompileStatic FunctionColumn STR_LEFT(Object column, Object length) { vColumn('str_left', column, length) }
    @CompileStatic FunctionColumn STR_REPLACE(Object column, Object checkStr, Object replaceStr) { vColumn('str_replace', column, checkStr, replaceStr) }
    @CompileStatic FunctionColumn STR_RIGHT(Object column, Object length) { vColumn('str_right', column, length) }
    @CompileStatic FunctionColumn SUBSTRING(Object column, Object start) { vColumn('substr', column, start) }
    @CompileStatic FunctionColumn SUBSTRING(Object column, Object start, Object length) { vColumn('substr', column, start, length) }
    @CompileStatic FunctionColumn TRIM(Object column)     { fColumn(column, 'trim') }
    @CompileStatic FunctionColumn UCASE(Object column)    { fColumn(column, 'ucase') }

    /*
        Math Functions
     */

    @CompileStatic FunctionColumn ABS(Object column) { fColumn(column, 'abs') }
    @CompileStatic FunctionColumn ACOS(Object column) { fColumn(column, 'trig_acos') }
    @CompileStatic FunctionColumn ASIN(Object column) { fColumn(column, 'trig_asin') }
    @CompileStatic FunctionColumn ATAN(Object column) { fColumn(column, 'trig_atan') }
    @CompileStatic FunctionColumn ATAN2(Object y, Object x) { vColumn('trig_atan2', y, x) }
    @CompileStatic FunctionColumn CEIL(Object column) { fColumn(column, 'ceil') }
    @CompileStatic FunctionColumn COS(Object column) { fColumn(column, 'trig_cos') }
    @CompileStatic FunctionColumn COT(Object column) { fColumn(column, 'trig_cot') }
    @CompileStatic FunctionColumn DEGREES(Object column) { fColumn(column, 'num_degrees') }
    @CompileStatic FunctionColumn EXP(Object column) { fColumn(column, 'lg_exp') }
    @CompileStatic FunctionColumn FLOOR(Object column) { fColumn(column, 'floor') }
    @CompileStatic FunctionColumn LOGE(Object column) { fColumn(column, 'lg_ln') }
    @CompileStatic FunctionColumn LOG(Object base, Object val) { vColumn('lg_log', base, val) }
    @CompileStatic FunctionColumn POWER(Object column, Object magnitude) { vColumn('power', column, magnitude) }
    @CompileStatic FunctionColumn RADIANS(Object column) { fColumn(column, 'num_radians') }
    @CompileStatic FunctionColumn ROUND(Object column, Object decimalPlaces)    { vColumn('round', column, decimalPlaces) }
    @CompileStatic FunctionColumn SIGN(Object column) { fColumn(column, 'num_sign') }
    @CompileStatic FunctionColumn SIN(Object column) { fColumn(column, 'trig_sin') }
    @CompileStatic FunctionColumn SQRT(Object column) { fColumn(column, 'sqrt') }
    @CompileStatic FunctionColumn TAN(Object column) { fColumn(column, 'trig_tan') }
    @CompileStatic FunctionColumn TRUNCATE(Object column, Object dpoints) { vColumn('truncate', column, dpoints) }

    // cast function
    @CompileStatic FunctionColumn CAST_INT(Object column) { fColumn(column, 'cast_to_int') }
    @CompileStatic FunctionColumn CAST_STR(Object column) { fColumn(column, 'cast_to_str') }
    @CompileStatic FunctionColumn CAST_DATE(Object column) { fColumn(column, 'cast_to_date') }

    /// Date time functions
    @CompileStatic FunctionColumn NOW() { fColumn(null, 'current_timestamp') }
    @CompileStatic FunctionColumn CURDATE() { fColumn(null, 'current_date') }
    @CompileStatic FunctionColumn CURRENT_DATE() { CURDATE() }
    @CompileStatic FunctionColumn CURTIME() { fColumn(null, 'current_time') }
    @CompileStatic FunctionColumn CURRENT_TIME() { CURTIME() }
    @CompileStatic FunctionColumn CUREPOCH() { fColumn(null, 'current_epoch') }
    @CompileStatic FunctionColumn CURRENT_EPOCH() { CUREPOCH() }
    @CompileStatic FunctionColumn DATE_TRUNC(Column column) { fColumn(column, 'date_trunc') }

    @CompileStatic FunctionColumn EPOCH_TO_DATE(Object column)     { fColumn(column, 'epoch_to_date') }
    @CompileStatic FunctionColumn EPOCH_TO_DATETIME(Object column)     { fColumn(column, 'epoch_to_datetime') }

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