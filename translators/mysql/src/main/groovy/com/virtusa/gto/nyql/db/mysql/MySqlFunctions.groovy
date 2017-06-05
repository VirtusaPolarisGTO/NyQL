package com.virtusa.gto.nyql.db.mysql

import com.virtusa.gto.nyql.QContextType
import com.virtusa.gto.nyql.db.AbstractSQLTranslator
import com.virtusa.gto.nyql.db.QFunctions
import com.virtusa.gto.nyql.db.TranslatorOptions
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.exceptions.NySyntaxException
import groovy.transform.CompileStatic
/**
 * @author Isuru Weerarathna
 */
abstract class MySqlFunctions extends AbstractSQLTranslator implements QFunctions {

    protected MySqlFunctions() {
        super()
    }

    MySqlFunctions(TranslatorOptions theOptions) {
        super(theOptions)
    }

    @CompileStatic
    @Override
    String truncate(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            return 'TRUNCATE(' + ___resolveIn(c.get(0), pmx) + ', ' + ___resolveIn(c.get(1), pmx) + ')'
        }
        throw new NyException('Incorrect number of parameters for string replace function!')
    }

    @CompileStatic
    @Override
    String date_trunc(it) { String.format('DATE(%s)', ___resolveInP(it)) }

    @CompileStatic
    @Override
    String str_replace(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            return 'REPLACE(' + ___resolveIn(c.get(0), pmx) + ', ' + ___resolveIn(c.get(1), pmx) + ', ' + ___resolveIn(c.get(2), pmx) + ')'
        }
        throw new NyException('Incorrect number of parameters for string replace function!')
    }

    @Override
    String substr(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            return 'SUBSTRING(' + ___resolveIn(c.get(0), pmx) + ', ' + ___resolveIn(c.get(1), pmx) +
                    (c.size() > 2 ? ', ' + ___resolveIn(c.get(2), pmx) : '') + ')'
        } else {
            throw new NyException('Insufficient parameters for SUBSTRING function!')
        }
    }

    @CompileStatic
    @Override
    String position(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            return String.format('POSITION(%s IN %s)', ___resolveIn(c.get(1), pmx), ___resolveIn(c.get(0), pmx))
        }
        throw new NyException('Insufficient parameters for POSITION function!')
    }

    @CompileStatic
    @Override
    String cast_to_int(Object col) {
        String.format('CAST(%s AS SIGNED)', ___resolveInP(col))
    }

    @CompileStatic
    @Override
    String cast_to_str(Object col) {
        String.format('CAST(%s AS CHAR)', ___resolveInP(col))
    }

    @CompileStatic
    @Override
    String cast_to_date(Object col) {
        String.format('CAST(%s AS DATE)', ___resolveInP(col))
    }

    @CompileStatic
    String date_diff_years(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('TIMESTAMPDIFF(YEAR, %s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw requireTwoParams()
    }

    @CompileStatic
    String date_diff_months(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('TIMESTAMPDIFF(MONTH, %s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw requireTwoParams()
    }

    @CompileStatic
    String date_diff_days(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('TIMESTAMPDIFF(DAY, %s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw requireTwoParams()
    }

    @CompileStatic
    String date_diff_weeks(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('TIMESTAMPDIFF(WEEK, %s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw requireTwoParams()
    }

    @CompileStatic
    String date_diff_hours(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('TIMESTAMPDIFF(HOUR, %s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw requireTwoParams()
    }

    @CompileStatic
    String date_diff_minutes(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('TIMESTAMPDIFF(MINUTE, %s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw requireTwoParams()
    }

    @CompileStatic
    String date_diff_seconds(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('TIMESTAMPDIFF(SECOND, %s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw requireTwoParams()
    }

    @CompileStatic
    String date_add_days(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('(%s + INTERVAL %s DAY)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw invalidDateAddSyntax()
    }

    @CompileStatic
    String date_add_months(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('(%s + INTERVAL %s MONTH)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw invalidDateAddSyntax()
    }

    @CompileStatic
    String date_add_years(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('(%s + INTERVAL %s YEAR)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw invalidDateAddSyntax()
    }

    @CompileStatic
    String date_add_weeks(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('(%s + INTERVAL %s WEEK)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw invalidDateAddSyntax()
    }

    @CompileStatic
    String date_add_hours(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('(%s + INTERVAL %s HOUR)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw invalidDateAddSyntax()
    }

    @CompileStatic
    String date_add_minutes(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('(%s + INTERVAL %s MINUTE)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw invalidDateAddSyntax()
    }

    @CompileStatic
    String date_add_seconds(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('(%s + INTERVAL %s SECOND)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw invalidDateAddSyntax()
    }

    @CompileStatic
    String date_sub_days(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('(%s - INTERVAL %s DAY)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw invalidDateSubSyntax()
    }

    @CompileStatic
    String date_sub_months(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('(%s - INTERVAL %s MONTH)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw invalidDateSubSyntax()
    }

    @CompileStatic
    String date_sub_years(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('(%s - INTERVAL %s YEAR)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw invalidDateSubSyntax()
    }

    @CompileStatic
    String date_sub_weeks(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('(%s - INTERVAL %s WEEK)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw invalidDateSubSyntax()
    }

    @CompileStatic
    String date_sub_hours(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('(%s - INTERVAL %s HOUR)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw invalidDateSubSyntax()
    }

    @CompileStatic
    String date_sub_minutes(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('(%s - INTERVAL %s MINUTE)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw invalidDateSubSyntax()
    }

    @CompileStatic
    String date_sub_seconds(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('(%s - INTERVAL %s SECOND)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw invalidDateSubSyntax()
    }

    @CompileStatic String current_epoch(it) { 'UNIX_TIMESTAMP() * 1000' }
    @CompileStatic String current_epoch() { current_epoch(null) }

    @CompileStatic
    String epoch_to_date(it) { String.format('DATE(FROM_UNIXTIME(%s / 1000))', ___resolveInP(it)) }

    @CompileStatic
    String epoch_to_datetime(it) { String.format('FROM_UNIXTIME(%s / 1000)', ___resolveInP(it)) }

    String mysql_cast(itx) {
        def it = ___val(itx)
        def pmx = ___pm(itx)
        if (it instanceof List)
            return 'CAST(' + ___resolve(it[0], QContextType.SELECT, pmx) + ' AS ' + ___resolve(it[1], QContextType.SELECT, pmx) + ')'
        else
            throw new NyException('CAST function expects two parameters!')
    }

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