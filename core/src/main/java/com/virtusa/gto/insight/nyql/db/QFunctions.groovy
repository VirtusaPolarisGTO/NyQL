package com.virtusa.gto.insight.nyql.db

import com.virtusa.gto.insight.nyql.FunctionColumn
import com.virtusa.gto.insight.nyql.QContextType
import com.virtusa.gto.insight.nyql.exceptions.NyException

import java.util.stream.Collectors

/**
 * @author IWEERARATHNA
 */
trait QFunctions {

    def current_timestamp   = { c -> return "NOW()" }
    def current_date        = { c -> return "CURDATE()" }
    def current_time        = { c -> return "CURTIME()" }

    def datediff            = { c ->
            if (c instanceof List) {
                return "DATEDIFF(" + ___resolve(c[0], QContextType.INSIDE_FUNCTION) + ", " +
                        ___resolve(c[1], QContextType.INSIDE_FUNCTION) + ")"
            } else {
                throw new NyException("Invalid syntax for DATEDIFF function!")
            }
    }

    def date_trunc          = { c -> return "DATE(" + ___resolve(c, QContextType.INSIDE_FUNCTION) + ")" }

    def date_add    = { c ->
            if (c instanceof List) {
                return "(" + ___resolve(c[0], QContextType.INSIDE_FUNCTION) + " + INTERVAL " +
                        ___resolve(c[1], QContextType.INSIDE_FUNCTION) + " " + String.valueOf(c[2]) + ")"
            } else {
                throw new NyException("Invalid syntax for DATE_ADD function!")
            }
    }

    def date_sub    = { c ->
            if (c instanceof List) {
                return "(" + ___resolve(c[0], QContextType.INSIDE_FUNCTION) + " - INTERVAL " +
                        ___resolve(c[1], QContextType.INSIDE_FUNCTION) + " " + String.valueOf(c[2]) + ")"
            } else {
                throw new NyException("Invalid syntax for DATE_ADD function!")
            }
    }

    def timestamp_add = {

    }

    def timestamp_sub = {

    }

    def lcase   = { c ->
        if (c instanceof String) return "LOWER($c)"
        else return "LOWER(" + ___resolve(c, QContextType.INSIDE_FUNCTION) + ")"
    }

    def ucase   = { c ->
        if (c instanceof String) return "UPPER($c)"
        else return "UPPER(" + ___resolve(c, QContextType.INSIDE_FUNCTION) + ")"
    }

    def trim   = { c ->
        if (c instanceof String) return "TRIM($c)"
        else return "TRIM(" + ___resolve(c, QContextType.INSIDE_FUNCTION) + ")"
    }

    def len     = { c ->
        if (c instanceof String) return "CHAR_LENGTH($c)"
        else return "CHAR_LENGTH(" + ___resolve(c, QContextType.INSIDE_FUNCTION) + ")"
    }

    def round   = { c ->
        if (c instanceof List) return "ROUND(" + ___resolve(c[0], QContextType.INSIDE_FUNCTION) + ", " + ___resolve(c[1], QContextType.INSIDE_FUNCTION) + ")"
        else throw new NyException("ROUND function requires two parameters!")
    }

    def between = { c ->
            if (c instanceof List) {
                return "BETWEEN " + ___resolve(c[0], QContextType.INSIDE_FUNCTION) + " AND " +
                        ___resolve(c[1], QContextType.INSIDE_FUNCTION) + " "
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

}