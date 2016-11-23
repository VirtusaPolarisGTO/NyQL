package com.virtusa.gto.nyql.db.mssql

import com.virtusa.gto.nyql.db.QFunctions
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.exceptions.NySyntaxException

/**
 * @author IWEERARATHNA
 */
class MSSqlFunctions implements QFunctions {

    @Override
    String str_lpad(Object c) {
        if (c instanceof List) {
            if (c.size() == 3) {
                return String.format('RIGHT(%s, %s)',
                        String.format('REPLICATE(%s, %s) + %s', ___resolveIn(c.get(2)), ___resolveIn(c.get(1)), ___resolveIn(c.get(0))),
                        ___resolveIn(c.get(1)))
            } else if (c.size() == 2) {
                return String.format('RIGHT(%s, %s)',
                        String.format('REPLICATE(\' \', %s) + %s', ___resolveIn(c.get(2)), ___resolveIn(c.get(1)), ___resolveIn(c.get(0))),
                        ___resolveIn(c.get(1)))
            }
        }
        throw new NyException('Insufficient parameters for right pad function!')
    }

    @Override
    String str_rpad(Object c) {
        if (c instanceof List) {
            if (c.size() == 3) {
                return String.format('LEFT(%s, %s)',
                        String.format('REPLICATE(%s, %s) + %s', ___resolveIn(c.get(2)), ___resolveIn(c.get(1)), ___resolveIn(c.get(0))),
                        ___resolveIn(c.get(1)))
            } else if (c.size() == 2) {
                return String.format('LEFT(%s, %s)',
                        String.format('REPLICATE(\' \', %s) + %s', ___resolveIn(c.get(2)), ___resolveIn(c.get(1)), ___resolveIn(c.get(0))),
                        ___resolveIn(c.get(1)))
            }
        }
        throw new NyException('Insufficient parameters for left pad function!')
    }

    @Override
    String date_trunc(Object it) {
        return 'CAST(' + ___resolveIn(it) + ' AS DATE)'
    }

    @Override
    String substr(Object c) {
        if (c instanceof List) {
            return 'SUBSTRING(' + ___resolveIn(c[0]) + ', ' + ___resolveIn(c[1]) +
                    (c.size() > 2 ? ', ' + ___resolveIn(c[2]) : '') + ')'
        }
    }

    @Override
    String position(Object c) {
        if (c instanceof List) {
            return 'CHARINDEX(' + ___resolveIn(c[0]) + ', ' + ___resolveIn(c[1]) + ')'
        }
    }

    @Override
    String cast_to_int(Object col) {
        return 'CAST(' + ___resolveIn(col) + ' AS INT)'
    }

    @Override
    String cast_to_date(Object col) {
        date_trunc(col)
    }

    @Override
    String cast_to_str(Object col) {
        return 'STR(' + ___resolveIn(col) + ')'
    }

    String date_diff_years(c) {
        if (c instanceof List) return "DATEDIFF(year, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_months(c) {
        if (c instanceof List) return "DATEDIFF(month, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_days(c) {
        if (c instanceof List) return "DATEDIFF(day, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_weeks(c) {
        if (c instanceof List) return "DATEDIFF(week, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_hours(c) {
        if (c instanceof List) return "DATEDIFF(hour, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_minutes(c) {
        if (c instanceof List) return "DATEDIFF(minute, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_seconds(c) {
        if (c instanceof List) return "DATEDIFF(second, " + ___resolveIn(c[0]) + ", " + ___resolveIn(c[1]) + ")"
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }

}
