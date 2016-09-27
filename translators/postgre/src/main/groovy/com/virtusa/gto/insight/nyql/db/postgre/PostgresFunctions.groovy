package com.virtusa.gto.insight.nyql.db.postgre

import com.virtusa.gto.insight.nyql.FunctionColumn
import com.virtusa.gto.insight.nyql.QContextType
import com.virtusa.gto.insight.nyql.db.QFunctions
import com.virtusa.gto.insight.nyql.exceptions.NyException

import java.util.stream.Collectors

/**
 * @author IWEERARATHNA
 */
trait PostgresFunctions implements QFunctions {

    def current_date        = { c -> return "CURRENT_TIME" }
    def current_time        = { c -> return "CURRENT_DATE" }

    def date_trunc          = { c -> return "DATE_TRUNC('day', " + ___resolve(c, QContextType.INSIDE_FUNCTION) + ")" }

    def date_add    = {
        c ->
            if (c instanceof List) {
                return "(" + ___resolve(c[0], QContextType.INSIDE_FUNCTION) + " + INTERVAL '" +
                        ___resolve(c[1], QContextType.INSIDE_FUNCTION) + " " + String.valueOf(c[2]) + "')"
            } else {
                throw new NyException("Invalid syntax for DATE_ADD function!")
            }
    }

    def date_sub    = {
        c ->
            if (c instanceof List) {
                return "(" + ___resolve(c[0], QContextType.INSIDE_FUNCTION) + " - INTERVAL '" +
                        ___resolve(c[1], QContextType.INSIDE_FUNCTION) + " " + String.valueOf(c[2]) + "')"
            } else {
                throw new NyException("Invalid syntax for DATE_ADD function!")
            }
    }

    def timestamp_add = {
        c ->
            if (c instanceof List) {
                return "(timestamp " + ___resolve(c[0], QContextType.INSIDE_FUNCTION) + " + timestamp " +
                        ___resolve(c[1], QContextType.INSIDE_FUNCTION) + ")"
            } else {
                throw new NyException("Invalid syntax for DATE_ADD function!")
            }
    }

    def timestamp_sub = {
        c ->
            if (c instanceof List) {
                return "(timestamp " + ___resolve(c[0], QContextType.INSIDE_FUNCTION) + " - timestamp " +
                        ___resolve(c[2], QContextType.INSIDE_FUNCTION) + ")"
            } else {
                throw new NyException("Invalid syntax for DATE_ADD function!")
            }
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
            }).collect(Collectors.joining(" || ", "(", ")"))
        }
    }

}