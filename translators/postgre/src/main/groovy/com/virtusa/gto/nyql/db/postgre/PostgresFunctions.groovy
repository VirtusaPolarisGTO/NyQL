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

    @CompileStatic String date_trunc(c) { String.format("DATE_TRUNC('day', %s)", ___resolveIn(c)) }

    @Override
    String str_replace(Object c) {
        if (c instanceof List) {
            return 'replace(' + ___resolveIn(c[0]) + ', ' + ___resolveIn(c[1]) + ', ' + ___resolveIn(c[2]) + ')'
        }
        throw new NyException('Incorrect number of parameters for string replace function!')
    }

    @Override
    String substr(Object c) {
        if (c instanceof List) {
            'SUBSTRING(' + ___resolveIn(c[0]) + ', FROM ' + ___resolveIn(c[1]) +
                    (c.size() > 2 ? ', FOR ' + ___resolveIn(c[2]) : '') + ')'
        } else {
            throw new NyException('Insufficient parameters for SUBSTRING function!')
        }
    }

    @Override
    String position(Object c) {
        if (c instanceof List) {
            String.format('POSITION(%s IN %s)', ___resolveIn(c.get(1)), ___resolveIn(c.get(0)))
        } else {
            throw new NyException('Insufficient parameters for POSITION function!')
        }
    }

    String date_diff_years(c) {
        if (c instanceof List) String.format("DATE_PART('year', %s) - DATE_PART('year', %s)", ___resolveIn(c[1]), ___resolveIn(c[0]))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_months(c) {
        if (c instanceof List) {
            String.format("%s * 12 + (DATE_PART('month', %s) - DATE_PART('month', %s))", date_diff_years(c), ___resolveIn(c[1]), ___resolveIn(c[0]))
        } else {
            throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
        }
    }
    String date_diff_days(c) {
        if (c instanceof List) String.format("DATE_PART('day', %s - %s)", ___resolveIn(c[1]), ___resolveIn(c[0]))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_weeks(c) {
        if (c instanceof List) String.format("TRUNC(DATE_PART('day', %s - %s) / 7)", ___resolveIn(c[1]), ___resolveIn(c[0]))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_hours(c) {
        if (c instanceof List) String.format("%s * 24 + DATE_PART('hour', %s - %s)", date_diff_days(c), ___resolveIn(c[1]), ___resolveIn(c[0]))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_minutes(c) {
        if (c instanceof List) String.format("%s * 60 + DATE_PART('minute', %s - %s)", date_diff_hours(c), ___resolveIn(c[1]), ___resolveIn(c[0]))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_seconds(c) {
        if (c instanceof List) String.format("%s * 60 + DATE_PART('minute', %s - %s)", date_diff_minutes(c), ___resolveIn(c[1]), ___resolveIn(c[0]))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }

    String date_add_days(c) {
        if (c instanceof List) String.format("(%s + INTERVAL '%s day'", ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_months(c) {
        if (c instanceof List) String.format("(%s + INTERVAL '%s month'", ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_years(c) {
        if (c instanceof List) String.format("(%s + INTERVAL '%s year'", ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_weeks(c) {
        if (c instanceof List) String.format("(%s + INTERVAL '%s week'", ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_hours(c) {
        if (c instanceof List) String.format("(%s + INTERVAL '%s hour'", ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_minutes(c) {
        if (c instanceof List) String.format("(%s + INTERVAL '%s minute'", ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_seconds(c) {
        if (c instanceof List) String.format("(%s + INTERVAL '%s second'", ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }

    String date_sub_days(c) {
        if (c instanceof List) String.format("(%s - INTERVAL '%s day'", ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_months(c) {
        if (c instanceof List) String.format("(%s - INTERVAL '%s month'", ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_years(c) {
        if (c instanceof List) String.format("(%s - INTERVAL '%s year'", ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_weeks(c) {
        if (c instanceof List) String.format("(%s - INTERVAL '%s week'", ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_hours(c) {
        if (c instanceof List) String.format("(%s - INTERVAL '%s hour'", ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_minutes(c) {
        if (c instanceof List) String.format("(%s - INTERVAL '%s minute'", ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_seconds(c) {
        if (c instanceof List) String.format("(%s - INTERVAL '%s second'", ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }

    @CompileStatic String current_epoch(it) { 'extract(epoch from now()) * 1000' }
    @CompileStatic String current_epoch() { current_epoch(null) }

    @CompileStatic String epoch_to_date(it) { epoch_to_datetime(it) + '::date' }
    @CompileStatic String epoch_to_datetime(it) {
        if (it instanceof QNumber || it instanceof Number) {
            String.format('to_timestamp(%s / 1000)', ___resolveIn(it))
        } else {
            String.format('to_timestamp(extract(epoch from %s))', ___resolveIn(it))
        }
    }

    String concat(c) {
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
                    return ___resolve(col, QContextType.INSIDE_FUNCTION)
                }
            }).collect(Collectors.joining(" || ", "(", ")"))
        }
    }

    @CompileStatic @Override
    String cast_to_int(Object col) {
        String.format('CAST(%s AS INTEGER)', ___resolveIn(col))
    }

    @CompileStatic @Override
    String cast_to_str(Object col) {
        String.format('CAST(%s AS TEXT)', ___resolveIn(col))
    }

    @CompileStatic @Override
    String cast_to_date(Object col) {
        String.format('to_date(%s, YYYY-MM-DD)', ___resolveIn(col))
    }

    @CompileStatic @Override
    String op_bit_xor(Object it) {
        if (it instanceof List) {
            String.format('%s # %s', ___resolveIn(it.get(0)), ___resolveIn(it.get(1)))
        } else {
            throw new NySyntaxException('Bitwise Xor operation requires at least two operands!')
        }
    }
}