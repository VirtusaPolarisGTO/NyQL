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
        String.format('CAST(%s AS DATE)', ___resolveIn(it))
    }

    @Override
    String substr(Object c) {
        if (c instanceof List) {
            'SUBSTRING(' + ___resolveIn(c[0]) + ', ' + ___resolveIn(c[1]) +
                    (c.size() > 2 ? ', ' + ___resolveIn(c[2]) : '') + ')'
        } else {
            throw new NySyntaxException('Insufficient parameters for SUBSTRING function!')
        }
    }

    @Override
    String position(Object c) {
        if (c instanceof List) {
            String.format('CHARINDEX(%s, %s)', ___resolveIn(c[0]), ___resolveIn(c[1]))
        } else {
            throw new NySyntaxException('Insufficient parameters for POSITION function!')
        }
    }

    @Override
    String cast_to_int(Object col) {
        String.format('CAST(%s AS INT)', ___resolveIn(col))
    }

    @Override
    String cast_to_date(Object col) {
        date_trunc(col)
    }

    @Override
    String cast_to_str(Object col) {
        String.format('STR(%s)', ___resolveIn(col))
    }

    @Override
    String current_epoch() {
        'CAST(DATEDIFF(SECOND, \'19700101\', GETUTCDATE()) AS BIGINT) * 1000'
    }

    @Override
    String epoch_to_date(Object col) {
        String.format('CAST(DATEADD(s, %s, \'19700101\') AS DATE)', ___resolveIn(col))
    }

    @Override
    String epoch_to_datetime(Object col) {
        String.format('DATEADD(s, %s, \'19700101\')', ___resolveIn(col))
    }

    String date_diff_years(c) {
        if (c instanceof List) String.format('DATEDIFF(year, %s, %s)', ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_months(c) {
        if (c instanceof List) String.format('DATEDIFF(month, %s, %s)', ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_days(c) {
        if (c instanceof List) String.format('DATEDIFF(day, %s, %s)', ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_weeks(c) {
        if (c instanceof List) String.format('DATEDIFF(week, %s, %s)', ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_hours(c) {
        if (c instanceof List) String.format('DATEDIFF(hour, %s, %s)', ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_minutes(c) {
        if (c instanceof List) String.format('DATEDIFF(minute, %s, %s)', ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }
    String date_diff_seconds(c) {
        if (c instanceof List) String.format('DATEDIFF(second, %s, %s)', ___resolveIn(c[0]), ___resolveIn(c[1]))
        else throw new NySyntaxException("DATE DIFF function requires exactly two parameters!")
    }

    String date_add_days(c) {
        if (c instanceof List) {
            String.format('DATEADD(day, %s, %s)', ___resolveIn(c[1]), ___resolveIn(c[0]))
        } else {
            throw new NySyntaxException('Date add function requires exactly two parameters!')
        }
    }
    String date_add_months(c) {
        if (c instanceof List) {
            String.format('DATEADD(month, %s, %s)', ___resolveIn(c[1]), ___resolveIn(c[0]))
        } else {
            throw new NySyntaxException('Date add function requires exactly two parameters!')
        }
    }
    String date_add_years(c) {
        if (c instanceof List) {
            String.format('DATEADD(year, %s, %s)', ___resolveIn(c[1]), ___resolveIn(c[0]))
        } else {
            throw new NySyntaxException('Date add function requires exactly two parameters!')
        }
    }
    String date_add_weeks(c) {
        if (c instanceof List) {
            String.format('DATEADD(week, %s, %s)', ___resolveIn(c[1]), ___resolveIn(c[0]))
        } else {
            throw new NySyntaxException('Date add function requires exactly two parameters!')
        }
    }
    String date_add_hours(c) {
        if (c instanceof List) {
            String.format('DATEADD(hour, %s, %s)', ___resolveIn(c[1]), ___resolveIn(c[0]))
        } else {
            throw new NySyntaxException('Date add function requires exactly two parameters!')
        }
    }
    String date_add_minutes(c) {
        if (c instanceof List) {
            String.format('DATEADD(minute, %s, %s)', ___resolveIn(c[1]), ___resolveIn(c[0]))
        } else {
            throw new NySyntaxException('Date add function requires exactly two parameters!')
        }
    }
    String date_add_seconds(c) {
        if (c instanceof List) {
            String.format('DATEADD(second, %s, %s)', ___resolveIn(c[1]), ___resolveIn(c[0]))
        } else {
            throw new NySyntaxException('Date add function requires exactly two parameters!')
        }
    }

    String date_sub_days(c) {
        if (c instanceof List) {
            String.format('DATEADD(day, -%s, %s)', ___resolveIn(c[1]), ___resolveIn(c[0]))
        } else {
            throw new NySyntaxException('Date subtract function requires exactly two parameters!')
        }
    }
    String date_sub_months(c) {
        if (c instanceof List) {
            String.format('DATEADD(month, -%s, %s)', ___resolveIn(c[1]), ___resolveIn(c[0]))
        } else {
            throw new NySyntaxException('Date subtract function requires exactly two parameters!')
        }
    }
    String date_sub_years(c) {
        if (c instanceof List) {
            String.format('DATEADD(year, -%s, %s)', ___resolveIn(c[1]), ___resolveIn(c[0]))
        } else {
            throw new NySyntaxException('Date subtract function requires exactly two parameters!')
        }
    }
    String date_sub_weeks(c) {
        if (c instanceof List) {
            String.format('DATEADD(week, -%s, %s)', ___resolveIn(c[1]), ___resolveIn(c[0]))
        } else {
            throw new NySyntaxException('Date subtract function requires exactly two parameters!')
        }
    }
    String date_sub_hours(c) {
        if (c instanceof List) {
            String.format('DATEADD(hour, -%s, %s)', ___resolveIn(c[1]), ___resolveIn(c[0]))
        } else {
            throw new NySyntaxException('Date subtract function requires exactly two parameters!')
        }
    }
    String date_sub_minutes(c) {
        if (c instanceof List) {
            String.format('DATEADD(minute, -%s, %s)', ___resolveIn(c[1]), ___resolveIn(c[0]))
        } else {
            throw new NySyntaxException('Date subtract function requires exactly two parameters!')
        }
    }
    String date_sub_seconds(c) {
        if (c instanceof List) {
            String.format('DATEADD(second, -%s, %s)', ___resolveIn(c[1]), ___resolveIn(c[0]))
        } else {
            throw new NySyntaxException('Date subtract function requires exactly two parameters!')
        }
    }
}
