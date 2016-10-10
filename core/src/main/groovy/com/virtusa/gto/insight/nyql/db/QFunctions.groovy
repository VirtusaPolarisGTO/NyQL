package com.virtusa.gto.insight.nyql.db

import com.virtusa.gto.insight.nyql.FunctionColumn
import com.virtusa.gto.insight.nyql.QContextType
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.exceptions.NySyntaxException
import com.virtusa.gto.insight.nyql.utils.QUtils

import java.util.stream.Collectors

/**
 * @author IWEERARATHNA
 */
trait QFunctions {

    def ___resolveIn(def obj) {
        return ___resolve(obj, QContextType.INSIDE_FUNCTION)
    }

    /**
     * Date and Time functions
     */
    def current_timestamp() { return 'NOW()' }
    def current_date() { return 'CURDATE()' }
    def current_time() { return 'CURTIME()' }
    def date_trunc(it) { 'DATE(' + ___resolveIn(it) + ')' }

    /**
     * String functions.
     */
    def lcase(c) { return 'LOWER(' + ___resolveIn(c) + ')' }
    def ucase(c) { return 'UPPER(' + ___resolveIn(c) + ')' }
    def trim(c) { return 'TRIM(' + ___resolveIn(c) + ')' }
    def len(c) { return 'CHAR_LENGTH(' + ___resolveIn(c) + ')' }

    /**
     * Math functions.
     */
    def round(c) {
        if (c instanceof List) return 'ROUND(' + ___resolveIn(c[0]) + ', ' + ___resolveIn(c[1]) + ')'
        else throw new NyException('ROUND function requires two parameters!')
    }

    def floor(c) { return 'FLOOR(' + ___resolveIn(c) + ')' }
    def ceil(c) { return 'CEILING(' + ___resolveIn(c) + ')' }
    def abs(c) { return 'ABS(' + ___resolveIn(c) + ')' }

    def distinct(c) { return 'DISTINCT ' + ___resolveIn(c) }

    def between(c) {
            if (c instanceof List) {
                return 'BETWEEN ' + ___resolveIn(c[0]) + ' AND ' + ___resolveIn(c[1])
            } else {
                throw new NyException('Invalid syntax for BETWEEN function!')
            }
    }

    def not_between = { c ->
        if (c instanceof List) {
            return "NOT BETWEEN " + ___resolve(c[0], QContextType.INSIDE_FUNCTION) + " AND " +
                    ___resolve(c[1], QContextType.INSIDE_FUNCTION)
        } else {
            throw new NyException("Invalid syntax for BETWEEN function!")
        }
    }

    def like = { c ->
        if (c instanceof List) { return "LIKE " + ___resolve(c[0], QContextType.INSIDE_FUNCTION) }
        else { return "LIKE " + ___resolve(c, QContextType.INSIDE_FUNCTION) }
    }

    def not_like = { c ->
        if (c instanceof List) { return "NOT LIKE " + ___resolve(c[0], QContextType.INSIDE_FUNCTION) }
        else { return "NOT LIKE " + ___resolve(c, QContextType.INSIDE_FUNCTION) }
    }

    def concat = {
        c -> if (c instanceof String) {
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
            }).collect(Collectors.joining(", ", "CONCAT(", ")"))
        }
    }

    def asc     = { c ->
        if (c instanceof String) return "$c ASC"
        else return ___resolve(c, QContextType.ORDER_BY) + " ASC"
    }

    def desc    = { c ->
        if (c instanceof String) return "$c DESC"
        else return ___resolve(c, QContextType.ORDER_BY) + " DESC"
    }

    def count   = { c ->
        c = c ?: "*"
        if (c instanceof String) return "COUNT($c)"
        else return "COUNT(" + ___resolve(c, QContextType.INSIDE_FUNCTION) + ")"
    }

    def count_distinct = { c ->
        if (c == null) return "COUNT(DISTINCT)"
        else return "COUNT(DISTINCT " + ___resolve(c, QContextType.INSIDE_FUNCTION) + ")"
    }

    def sum   = { c ->
        c = c ?: "*"
        if (c instanceof String) return "SUM($c)"
        else return "SUM(" + ___resolve(c, QContextType.INSIDE_FUNCTION) + ")"
    }

    def avg   = { c ->
        c = c ?: "*"
        if (c instanceof String) return "AVG($c)"
        else return "AVG(" + ___resolve(c, QContextType.INSIDE_FUNCTION) + ")"
    }

    def min   = { c ->
        c = c ?: "*"
        if (c instanceof String) return "MIN($c)"
        else return "MIN(" + ___resolve(c, QContextType.INSIDE_FUNCTION) + ")"
    }

    def max   = { c ->
        c = c ?: "*"
        if (c instanceof String) return "MAX($c)"
        else return "MAX(" + ___resolve(c, QContextType.INSIDE_FUNCTION) + ")"
    }

    def op_add = {
        if (it instanceof List) {
            List items = []
            QUtils.expandToList(items, it)
            items.stream().map({ op -> return ___resolve(op, QContextType.INSIDE_FUNCTION) })
                    .collect(Collectors.joining(" + ", "(", ")"))
        } else {
            throw new NySyntaxException("Add operation requires at least two operands!")
        }
    }

    def op_minus = {
        if (it instanceof List) {
            return "(" + ___resolve(it[0], QContextType.INSIDE_FUNCTION) +
                    " - " + ___resolve(it[1], QContextType.INSIDE_FUNCTION) + ")"
        } else {
            throw new NySyntaxException("Add operation requires at least two operands!")
        }
    }

    def op_multiply = {
        if (it instanceof List) {
            return "(" + ___resolve(it[0], QContextType.INSIDE_FUNCTION) +
                    " * " + ___resolve(it[1], QContextType.INSIDE_FUNCTION) + ")"
        } else {
            throw new NySyntaxException("Add operation requires at least two operands!")
        }
    }

    def op_divide = {
        if (it instanceof List) {
            return "(" + ___resolve(it[0], QContextType.INSIDE_FUNCTION) +
                    " / " + ___resolve(it[1], QContextType.INSIDE_FUNCTION) + ")"
        } else {
            throw new NySyntaxException("Add operation requires at least two operands!")
        }
    }

    def op_modulus = {
        if (it instanceof List) {
            return "(" + ___resolve(it[0], QContextType.INSIDE_FUNCTION) +
                    " % " + ___resolve(it[1], QContextType.INSIDE_FUNCTION) + ")"
        } else {
            throw new NySyntaxException("Add operation requires at least two operands!")
        }
    }
}