package com.virtusa.gto.insight.nyql.db.mssql

import com.virtusa.gto.insight.nyql.db.QFunctions
import com.virtusa.gto.insight.nyql.exceptions.NySyntaxException

/**
 * @author IWEERARATHNA
 */
trait MSSqlFunctions implements QFunctions {

    def date_diff_years(c) {
        if (c instanceof List) return "DATEDIFF(year, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    def date_diff_months(c) {
        if (c instanceof List) return "DATEDIFF(month, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    def date_diff_days(c) {
        if (c instanceof List) return "DATEDIFF(day, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    def date_diff_weeks(c) {
        if (c instanceof List) return "DATEDIFF(week, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    def date_diff_hours(c) {
        if (c instanceof List) return "DATEDIFF(hour, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    def date_diff_minutes(c) {
        if (c instanceof List) return "DATEDIFF(minute, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    def date_diff_seconds(c) {
        if (c instanceof List) return "DATEDIFF(second, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }

}
