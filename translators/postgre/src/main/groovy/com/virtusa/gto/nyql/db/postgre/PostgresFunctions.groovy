package com.virtusa.gto.nyql.db.postgre

import com.virtusa.gto.nyql.FunctionColumn
import com.virtusa.gto.nyql.QContextType
import com.virtusa.gto.nyql.db.AbstractSQLTranslator
import com.virtusa.gto.nyql.db.QFunctions
import com.virtusa.gto.nyql.db.TranslatorOptions
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.exceptions.NySyntaxException
import com.virtusa.gto.nyql.model.units.QNumber
import groovy.transform.CompileStatic

import java.util.stream.Collectors

/**
 * @author IWEERARATHNA
 */
abstract class PostgresFunctions extends AbstractSQLTranslator implements QFunctions {

    PostgresFunctions() {
        super()
    }

    PostgresFunctions(TranslatorOptions theOptions) {
        super(theOptions)
    }

    @CompileStatic String current_date(c) { 'CURRENT_TIME' }
    @CompileStatic String current_time(c) { 'CURRENT_DATE' }

    @CompileStatic String date_trunc(c) { String.format("DATE_TRUNC('day', %s)", ___resolveInP(c)) }

    @Override
    String str_replace(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            return 'replace(' + ___resolveIn(c[0], pmx) + ', ' + ___resolveIn(c[1], pmx) + ', ' + ___resolveIn(c[2], pmx) + ')'
        }
        throw new NyException('Incorrect number of parameters for string replace function!')
    }

    @Override
    String str_repeat(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            return 'repeat(' + ___resolveIn(c[0], pmx) + ', ' + ___resolveIn(c[1], pmx) + ')'
        }
        throw new NyException('Incorrect number of parameters for string repeat function')
    }

    @Override
    String substr(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            'SUBSTRING(' + ___resolveIn(c[0], pmx) + ', FROM ' + ___resolveIn(c[1], pmx) +
                    (c.size() > 2 ? ', FOR ' + ___resolveIn(c[2], pmx) : '') + ')'
        } else {
            throw new NyException('Insufficient parameters for SUBSTRING function!')
        }
    }

    @Override
    String position(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            String.format('POSITION(%s IN %s)', ___resolveIn(c.get(1), pmx), ___resolveIn(c.get(0), pmx))
        } else {
            throw new NyException('Insufficient parameters for POSITION function!')
        }
    }

    @Override
    String position_last(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            String tmp = ___resolveIn(c.get(0), pmx)
            String.format('LENGTH(%s) - POSITION(%s IN REVERSE(%s))', tmp, ___resolveIn(c.get(1), pmx), tmp)
        } else {
            throw new NyException('Insufficient parameters for POSITION function!')
        }
    }

    String date_diff_years(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("DATE_PART('year', %s) - DATE_PART('year', %s)", ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_months(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            String.format("%s * 12 + (DATE_PART('month', %s) - DATE_PART('month', %s))", date_diff_years(cx), ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        } else {
            throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
        }
    }
    String date_diff_days(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("DATE_PART('day', %s - %s)", ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_weeks(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("TRUNC(DATE_PART('day', %s - %s) / 7)", ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_hours(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("%s * 24 + DATE_PART('hour', %s - %s)", date_diff_days(cx), ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_minutes(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("%s * 60 + DATE_PART('minute', %s - %s)", date_diff_hours(cx), ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_seconds(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("%s * 60 + DATE_PART('minute', %s - %s)", date_diff_minutes(cx), ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }

    String date_add_days(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("(%s + INTERVAL '%s day'", ___resolveIn(c[0], pmx), ___resolveIn(c[1], pmx))
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_months(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("(%s + INTERVAL '%s month'", ___resolveIn(c[0], pmx), ___resolveIn(c[1], pmx))
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_years(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("(%s + INTERVAL '%s year'", ___resolveIn(c[0], pmx), ___resolveIn(c[1], pmx))
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_weeks(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("(%s + INTERVAL '%s week'", ___resolveIn(c[0], pmx), ___resolveIn(c[1], pmx))
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_hours(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("(%s + INTERVAL '%s hour'", ___resolveIn(c[0], pmx), ___resolveIn(c[1], pmx))
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_minutes(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("(%s + INTERVAL '%s minute'", ___resolveIn(c[0], pmx), ___resolveIn(c[1], pmx))
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_seconds(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("(%s + INTERVAL '%s second'", ___resolveIn(c[0], pmx), ___resolveIn(c[1], pmx))
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }

    String date_sub_days(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("(%s - INTERVAL '%s day'", ___resolveIn(c[0], pmx), ___resolveIn(c[1], pmx))
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_months(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("(%s - INTERVAL '%s month'", ___resolveIn(c[0], pmx), ___resolveIn(c[1], pmx))
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_years(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("(%s - INTERVAL '%s year'", ___resolveIn(c[0], pmx), ___resolveIn(c[1], pmx))
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_weeks(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("(%s - INTERVAL '%s week'", ___resolveIn(c[0], pmx), ___resolveIn(c[1], pmx))
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_hours(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("(%s - INTERVAL '%s hour'", ___resolveIn(c[0], pmx), ___resolveIn(c[1], pmx))
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_minutes(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("(%s - INTERVAL '%s minute'", ___resolveIn(c[0], pmx), ___resolveIn(c[1], pmx))
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_seconds(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format("(%s - INTERVAL '%s second'", ___resolveIn(c[0], pmx), ___resolveIn(c[1], pmx))
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }

    @CompileStatic String current_epoch(it) { 'extract(epoch from now()) * 1000' }
    @CompileStatic String current_epoch() { current_epoch(null) }

    @CompileStatic String epoch_to_date(it) { epoch_to_datetime(it) + '::date' }
    @CompileStatic String epoch_to_datetime(itx) {
        def it = ___val(itx)
        def pmx = ___pm(itx)
        if (it instanceof QNumber || it instanceof Number) {
            String.format('to_timestamp(%s / 1000)', ___resolveIn(it, pmx))
        } else {
            String.format('to_timestamp(extract(epoch from %s))', ___resolveIn(it, pmx))
        }
    }

    String concat(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof String) {
            return String.valueOf(c)
        } else {
            def list
            if (c instanceof FunctionColumn) {
                list = c._columns
            } else if (c instanceof List) {
                list = c
            } else {
                return null
            }
            return list.stream().map({
                col -> if (col instanceof String) {
                    return String.valueOf(col)
                } else {
                    return ___resolve(col, QContextType.INSIDE_FUNCTION, pmx)
                }
            }).collect(Collectors.joining(" || ", "(", ")"))
        }
    }

    @CompileStatic @Override
    String cast_to_int(Object col) {
        String.format('CAST(%s AS INTEGER)', ___resolveInP(col))
    }

    @CompileStatic @Override
    String cast_to_str(Object col) {
        String.format('CAST(%s AS TEXT)', ___resolveInP(col))
    }

    @CompileStatic @Override
    String cast_to_date(Object col) {
        String.format('to_date(%s, YYYY-MM-DD)', ___resolveInP(col))
    }

    @CompileStatic @Override
    String op_bit_xor(Object itx) {
        def it = ___val(itx)
        def pmx = ___pm(itx)
        if (it instanceof List) {
            String.format('%s # %s', ___resolveIn(it.get(0), pmx), ___resolveIn(it.get(1), pmx))
        } else {
            throw new NySyntaxException('Bitwise Xor operation requires at least two operands!')
        }
    }
}