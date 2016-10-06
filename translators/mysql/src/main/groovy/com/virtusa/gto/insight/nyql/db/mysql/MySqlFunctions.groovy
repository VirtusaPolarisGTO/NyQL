package com.virtusa.gto.insight.nyql.db.mysql

import com.virtusa.gto.insight.nyql.Column
import com.virtusa.gto.insight.nyql.FunctionColumn
import com.virtusa.gto.insight.nyql.QContextType
import com.virtusa.gto.insight.nyql.db.QFunctions
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.exceptions.NySyntaxException

/**
 * @author Isuru Weerarathna
 */
trait MySqlFunctions implements QFunctions {

    def date_diff_years(c) {
        if (c instanceof List) return "TIMESTAMPDIFF(YEAR, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    def date_diff_months(c) {
        if (c instanceof List) return "TIMESTAMPDIFF(MONTH, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    def date_diff_days(c) {
        if (c instanceof List) return "TIMESTAMPDIFF(DAY, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    def date_diff_weeks(c) {
        if (c instanceof List) return "TIMESTAMPDIFF(WEEK, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    def date_diff_hours(c) {
        if (c instanceof List) return "TIMESTAMPDIFF(HOUR, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    def date_diff_minutes(c) {
        if (c instanceof List) return "TIMESTAMPDIFF(MINUTE, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    def date_diff_seconds(c) {
        if (c instanceof List) return "TIMESTAMPDIFF(SECOND, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }

    def date_add_days(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " + INTERVAL " + ___resolveIn(c[1]) + " DAY)"
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    def date_add_months(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " + INTERVAL " + ___resolveIn(c[1]) + " MONTH)"
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    def date_add_years(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " + INTERVAL " + ___resolveIn(c[1]) + " YEAR)"
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    def date_add_weeks(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " + INTERVAL " + ___resolveIn(c[1]) + " WEEK)"
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    def date_add_hours(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " + INTERVAL " + ___resolveIn(c[1]) + " HOUR)"
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    def date_add_minutes(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " + INTERVAL " + ___resolveIn(c[1]) + " MINUTE)"
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }
    def date_add_seconds(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " + INTERVAL " + ___resolveIn(c[1]) + " SECOND)"
        else throw new NyException("Invalid syntax for DATE_ADD function!")
    }

    def date_sub_days(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " - INTERVAL " + ___resolveIn(c[1]) + " DAY)"
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    def date_sub_months(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " - INTERVAL " + ___resolveIn(c[1]) + " MONTH)"
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    def date_sub_years(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " - INTERVAL " + ___resolveIn(c[1]) + " YEAR)"
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    def date_sub_weeks(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " - INTERVAL " + ___resolveIn(c[1]) + " WEEK)"
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    def date_sub_hours(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " - INTERVAL " + ___resolveIn(c[1]) + " HOUR)"
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    def date_sub_minutes(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " - INTERVAL " + ___resolveIn(c[1]) + " MINUTE)"
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }
    def date_sub_seconds(c) {
        if (c instanceof List) return "(" + ___resolveIn(c[0]) + " - INTERVAL " + ___resolveIn(c[1]) + " SECOND)"
        else throw new NyException("Invalid syntax for DATE_SUB function!")
    }

    def current_epoch(it) { return "UNIX_TIMESTAMP() * 1000" }
    def current_epoch() { current_epoch(null) }

    def epoch_to_date(it) { return "DATE(FROM_UNIXTIME(" + ___resolveIn(it) + " / 1000))" }
    def epoch_to_datetime(it) { return "FROM_UNIXTIME(" + ___resolveIn(it) + " / 1000)" }

    String mysql_cast(it) {
            if (it instanceof List)
                return "CAST(" + ___resolve(it[0], QContextType.SELECT) + " AS " + ___resolve(it[1], QContextType.SELECT) + ")"
            else
                throw new NyException("CAST function expects two parameters!")
    }

    def CAST(Column source, Object toType) {
        return vColumn("mysql_cast", source, toType)
    }

    private FunctionColumn vColumn(String fName, Object... columns) {
        List<Object> vals = new ArrayList<>()
        vals.addAll(columns)
        return new FunctionColumn(_columns: vals, _func: fName, _setOfCols: true, _ctx: _ctx)
    }

    private FunctionColumn fColumn(Column column, String fName) {
        return new FunctionColumn(_wrapper: column, _func: fName, _ctx: _ctx)
    }

}