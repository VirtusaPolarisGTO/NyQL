package com.virtusa.gto.insight.nyql.db.postgre

import com.virtusa.gto.insight.nyql.FunctionColumn
import com.virtusa.gto.insight.nyql.QContextType
import com.virtusa.gto.insight.nyql.db.QFunctions
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.exceptions.NySyntaxException
import com.virtusa.gto.insight.nyql.model.units.QNumber

import java.util.stream.Collectors

/**
 * @author IWEERARATHNA
 */
class PostgresFunctions implements QFunctions {

    String current_date(c) { return "CURRENT_TIME" }
    String current_time(c) { return "CURRENT_DATE" }

    String date_trunc(c) { return "DATE_TRUNC('day', " + ___resolveIn(c) + ")" }

    @Override
    String substr(Object c) {
        if (c instanceof List) {
            return 'SUBSTRING(' + ___resolveIn(c[0]) + ', FROM ' + ___resolveIn(c[1]) +
                    (c.size() > 2 ? ', FOR ' + ___resolveIn(c[2]) : '') + ')'
        }
    }

    @Override
    String position(Object c) {
        if (c instanceof List) {
            return 'POSITION(' + ___resolveIn(c[1]) + ' IN ' + ___resolveIn(c[0]) + ')'
        }
    }

    String date_diff_years(c) {
        if (c instanceof List) return "DATE_PART('year', " + ___resolveIn(c[0]) + ") - DATE_PART('year', " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_months(c) {
        if (c instanceof List) return date_diff_years(c) + "* 12 + DATE_PART('month', " + ___resolveIn(c[0]) + ") - DATE_PART('month', " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_days(c) {
        if (c instanceof List) return "DATE_PART('day', " + ___resolveIn(c[1]) + " - " + ___resolveIn(c[0]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_weeks(c) {
        if (c instanceof List) return "TRUNC(DATE_PART('day', " + ___resolveIn(c[1]) + " - " + ___resolveIn(c[0]) + ") / 7)"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_hours(c) {
        if (c instanceof List) return date_diff_days(c) + " * 24 + DATE_PART('hour', " + ___resolveIn(c[1]) + " - " + ___resolveIn(c[0]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_minutes(c) {
        if (c instanceof List) return date_diff_hours(c) + " * 60 + DATE_PART('minute', " + ___resolveIn(c[1]) + " - " + ___resolveIn(c[0]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_seconds(c) {
        if (c instanceof List) return date_diff_minutes(c) + " * 60 + DATE_PART('minute', " + ___resolveIn(c[1]) + " - " + ___resolveIn(c[0]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }

    String date_add_days(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " + INTERVAL '" + ___resolveIn(c[1]) + " day')"
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_months(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " + INTERVAL '" + ___resolveIn(c[1]) + " month')"
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_years(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " + INTERVAL '" + ___resolveIn(c[1]) + " year')"
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_weeks(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " + INTERVAL '" + ___resolveIn(c[1]) + " week')"
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_hours(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " + INTERVAL '" + ___resolveIn(c[1]) + " hour')"
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_minutes(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " + INTERVAL '" + ___resolveIn(c[1]) + " minute')"
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    String date_add_seconds(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " + INTERVAL '" + ___resolveIn(c[1]) + " second')"
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }

    String date_sub_days(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " - INTERVAL '" + ___resolveIn(c[1]) + " day')"
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_months(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " - INTERVAL '" + ___resolveIn(c[1]) + " month')"
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_years(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " - INTERVAL '" + ___resolveIn(c[1]) + " year')"
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_weeks(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " - INTERVAL '" + ___resolveIn(c[1]) + " week')"
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_hours(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " - INTERVAL '" + ___resolveIn(c[1]) + " hour')"
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_minutes(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " - INTERVAL '" + ___resolveIn(c[1]) + " minute')"
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    String date_sub_seconds(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " - INTERVAL '" + ___resolveIn(c[1]) + " second')"
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }

    String current_epoch(it) { return "extract(epoch from now()) * 1000" }
    String current_epoch() { current_epoch(null) }

    String epoch_to_date(it) { return epoch_to_datetime(it) + "::date" }
    String epoch_to_datetime(it) {
        if (it instanceof QNumber || it instanceof Number) {
            return "to_timestamp(" + ___resolveIn(it) + " / 1000)"
        } else {
            return "to_timestamp(extract(epoch from " + ___resolveIn(it) + "))"
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

    @Override
    String cast_to_int(Object col) {
        return 'CAST(' + ___resolveIn(col) + ' AS INTEGER)'
    }

    @Override
    String cast_to_str(Object col) {
        return 'CAST(' + ___resolveIn(col) + ' AS TEXT)'
    }

    @Override
    String op_bit_xor(Object it) {
        if (it instanceof List) {
            return ___resolveIn(it[0]) + ' # ' + ___resolveIn(it[1])
        } else {
            throw new NySyntaxException('Bitwise Xor operation requires at least two operands!')
        }
    }
}