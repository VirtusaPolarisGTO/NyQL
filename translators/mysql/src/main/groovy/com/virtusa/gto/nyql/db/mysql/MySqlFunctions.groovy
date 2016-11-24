package com.virtusa.gto.nyql.db.mysql

import com.virtusa.gto.nyql.Column
import com.virtusa.gto.nyql.FunctionColumn
import com.virtusa.gto.nyql.QContextType
import com.virtusa.gto.nyql.db.AbstractSQLTranslator
import com.virtusa.gto.nyql.db.QFunctions
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.exceptions.NySyntaxException
import groovy.transform.CompileStatic

/**
 * @author Isuru Weerarathna
 */
abstract class MySqlFunctions extends AbstractSQLTranslator implements QFunctions {

    @CompileStatic
    @Override
    String date_trunc(it) { String.format('DATE(%s)', ___resolveIn(it)) }

    @CompileStatic
    @Override
    String substr(Object c) {
        if (c instanceof List) {
            return 'SUBSTRING(' + ___resolveIn(c.get(0)) + ', ' + ___resolveIn(c.get(1)) +
                    (c.size() > 2 ? ', ' + ___resolveIn(c.get(2)) : '') + ')'
        } else {
            throw new NyException('Insufficient parameters for SUBSTRING function!')
        }
    }

    @CompileStatic
    @Override
    String position(Object c) {
        if (c instanceof List) {
            return String.format('POSITION(%s IN %s)', ___resolveIn(c.get(1)), ___resolveIn(c.get(0)))
        }
        throw new NyException('Insufficient parameters for POSITION function!')
    }

    @CompileStatic
    @Override
    String cast_to_int(Object col) {
        String.format('CAST(%s AS SIGNED)', ___resolveIn(col))
    }

    @CompileStatic
    @Override
    String cast_to_str(Object col) {
        String.format('CAST(%s AS CHAR)', ___resolveIn(col))
    }

    @CompileStatic
    @Override
    String cast_to_date(Object col) {
        String.format('CAST(%s AS DATE)', ___resolveIn(col))
    }

    @CompileStatic
    String date_diff_years(c) {
        if (c instanceof List) String.format('TIMESTAMPDIFF(YEAR, %s, %s)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw requireTwoParams()
    }

    @CompileStatic
    String date_diff_months(c) {
        if (c instanceof List) String.format('TIMESTAMPDIFF(MONTH, %s, %s)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw requireTwoParams()
    }

    @CompileStatic
    String date_diff_days(c) {
        if (c instanceof List) String.format('TIMESTAMPDIFF(DAY, %s, %s)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw requireTwoParams()
    }

    @CompileStatic
    String date_diff_weeks(c) {
        if (c instanceof List) String.format('TIMESTAMPDIFF(WEEK, %s, %s)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw requireTwoParams()
    }

    @CompileStatic
    String date_diff_hours(c) {
        if (c instanceof List) String.format('TIMESTAMPDIFF(HOUR, %s, %s)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw requireTwoParams()
    }

    @CompileStatic
    String date_diff_minutes(c) {
        if (c instanceof List) String.format('TIMESTAMPDIFF(MINUTE, %s, %s)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw requireTwoParams()
    }

    @CompileStatic
    String date_diff_seconds(c) {
        if (c instanceof List) String.format('TIMESTAMPDIFF(SECOND, %s, %s)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw requireTwoParams()
    }

    @CompileStatic
    String date_add_days(c) {
        if (c instanceof List) String.format('(%s + INTERVAL %s DAY)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw invalidDateAddSyntax()
    }

    @CompileStatic
    String date_add_months(c) {
        if (c instanceof List) String.format('(%s + INTERVAL %s MONTH)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw invalidDateAddSyntax()
    }

    @CompileStatic
    String date_add_years(c) {
        if (c instanceof List) String.format('(%s + INTERVAL %s YEAR)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw invalidDateAddSyntax()
    }

    @CompileStatic
    String date_add_weeks(c) {
        if (c instanceof List) String.format('(%s + INTERVAL %s WEEK)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw invalidDateAddSyntax()
    }

    @CompileStatic
    String date_add_hours(c) {
        if (c instanceof List) String.format('(%s + INTERVAL %s HOUR)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw invalidDateAddSyntax()
    }

    @CompileStatic
    String date_add_minutes(c) {
        if (c instanceof List) String.format('(%s + INTERVAL %s MINUTE)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw invalidDateAddSyntax()
    }

    @CompileStatic
    String date_add_seconds(c) {
        if (c instanceof List) String.format('(%s + INTERVAL %s SECOND)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw invalidDateAddSyntax()
    }

    @CompileStatic
    String date_sub_days(c) {
        if (c instanceof List) String.format('(%s - INTERVAL %s DAY)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw invalidDateSubSyntax()
    }

    @CompileStatic
    String date_sub_months(c) {
        if (c instanceof List) String.format('(%s - INTERVAL %s MONTH)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw invalidDateSubSyntax()
    }

    @CompileStatic
    String date_sub_years(c) {
        if (c instanceof List) String.format('(%s - INTERVAL %s YEAR)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw invalidDateSubSyntax()
    }

    @CompileStatic
    String date_sub_weeks(c) {
        if (c instanceof List) String.format('(%s - INTERVAL %s WEEK)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw invalidDateSubSyntax()
    }

    @CompileStatic
    String date_sub_hours(c) {
        if (c instanceof List) String.format('(%s - INTERVAL %s HOUR)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw invalidDateSubSyntax()
    }

    @CompileStatic
    String date_sub_minutes(c) {
        if (c instanceof List) String.format('(%s - INTERVAL %s MINUTE)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw invalidDateSubSyntax()
    }

    @CompileStatic
    String date_sub_seconds(c) {
        if (c instanceof List) String.format('(%s - INTERVAL %s SECOND)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        else throw invalidDateSubSyntax()
    }

    @CompileStatic String current_epoch(it) { 'UNIX_TIMESTAMP() * 1000' }
    @CompileStatic String current_epoch() { current_epoch(null) }

    @CompileStatic
    String epoch_to_date(it) { String.format('DATE(FROM_UNIXTIME(%s / 1000))', ___resolveIn(it)) }

    @CompileStatic
    String epoch_to_datetime(it) { String.format('FROM_UNIXTIME(%s / 1000)', ___resolveIn(it)) }

    String mysql_cast(it) {
            if (it instanceof List)
                return 'CAST(' + ___resolve(it[0], QContextType.SELECT) + ' AS ' + ___resolve(it[1], QContextType.SELECT) + ')'
            else
                throw new NyException('CAST function expects two parameters!')
    }

    @CompileStatic
    FunctionColumn CAST(Column source, Object toType) {
        return vColumn('mysql_cast', source, toType)
    }

    private FunctionColumn vColumn(String fName, Object... columns) {
        List<Object> vals = new ArrayList<>()
        vals.addAll(columns)
        return new FunctionColumn(_columns: vals, _func: fName, _setOfCols: true, _ctx: _ctx)
    }

    /*
    private FunctionColumn fColumn(Column column, String fName) {
        return new FunctionColumn(_wrapper: column, _func: fName, _ctx: _ctx)
    }
    */

    @CompileStatic
    private static NyException requireTwoParams() {
        new NySyntaxException('DATE DIFF function requires exactly two parameters!')
    }

    @CompileStatic
    private static NySyntaxException invalidSyntax(String func='') {
        new NySyntaxException('Invalid syntax for ' + func + ' function!')
    }

    @CompileStatic
    private static NySyntaxException invalidDateAddSyntax() {
        invalidSyntax('DATE_ADD')
    }

    @CompileStatic
    private static NySyntaxException invalidDateSubSyntax() {
        invalidSyntax('DATE_SUB')
    }
}