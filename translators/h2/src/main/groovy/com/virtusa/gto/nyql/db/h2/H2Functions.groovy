package com.virtusa.gto.nyql.db.h2

import com.virtusa.gto.nyql.db.AbstractSQLTranslator
import com.virtusa.gto.nyql.db.QFunctions
import com.virtusa.gto.nyql.db.TranslatorOptions
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.exceptions.NySyntaxException
import groovy.transform.CompileStatic

/**
 * @author iweerarathna
 */
abstract class H2Functions extends AbstractSQLTranslator implements QFunctions {

    private static final String TIME_EPOCH_START = "PARSEDATETIME('1970-01-01T00:00:00Z', 'yyyy-MM-dd''T''HH:mm:ss''Z''')"

    H2Functions() {
        super()
    }

    H2Functions(TranslatorOptions theOptions) {
        super(theOptions)
    }

    @Override
    String date_format(Object pms) {
        def c = ___val(pms)
        def pmx = ___pm(pms)

        if (c instanceof List) {
            return String.format('FORMATDATETIME(%s, %s)', ___resolveIn(c[0], pmx), ___resolveIn(c[1], pmx))
        } else {
            throw new NySyntaxException('DATE_FORMAT function requires at least two parameters!')
        }
    }


    @Override
    String greatest(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)

        if (c instanceof List) {
            return 'GREATEST' + ___resolveIn(c, pmx)
        } else {
            throw new NySyntaxException('GREATEST function requires at least two or more values!')
        }
    }

    @Override
    String least(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)

        if (c instanceof List) {
            return 'LEAST' + ___resolveIn(c, pmx)
        } else {
            throw new NySyntaxException('LEAST function requires at least two or more values!')
        }
    }

    @Override
    String str_replace(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            return 'REPLACE(' + ___resolveIn(c.get(0), pmx) + ', ' + ___resolveIn(c.get(1), pmx) + ', ' + ___resolveIn(c.get(2), pmx) + ')'
        }
        throw new NyException('Incorrect number of parameters for string replace function!')
    }

    @Override
    String str_repeat(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            return 'REPEAT(' + ___resolveIn(c.get(0), pmx) + ', ' + ___resolveIn(c.get(1), pmx) + ')'
        }
        throw new NyException('Incorrect number of parameters for string repeat function')
    }

    @Override
    String substr(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            return 'SUBSTRING(' + ___resolveIn(c.get(0), pmx) + ', ' + ___resolveIn(c.get(1), pmx) +
                    (c.size() > 2 ? ', ' + ___resolveIn(c.get(2), pmx) : '') + ')'
        } else {
            throw new NyException('Insufficient parameters for SUBSTRING function!')
        }
    }

    @Override
    String truncate(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            return 'TRUNCATE(' + ___resolveIn(c.get(0), pmx) + ', ' + ___resolveIn(c.get(1), pmx) + ')'
        }
        throw new NyException('Incorrect number of parameters for truncate function!')
    }

    @Override
    String position(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            return String.format('POSITION(%s, %s)', ___resolveIn(c.get(1), pmx), ___resolveIn(c.get(0), pmx))
        }
        throw new NyException('Insufficient parameters for POSITION function!')
    }

    @Override
    String position_last(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            String tmp = ___resolveIn(c.get(0), pmx)
            return String.format('LENGTH(%s) - LOCATE(%s, NYQL_REVERSE(%s))', tmp, ___resolveIn(c.get(1), pmx), tmp)
        }
        throw new NyException('Insufficient parameters for POSITION function!')
    }

    @Override
    String str_reverse(Object c) {
        // reverse function is using a nyql function embedded into the database.
        String.format('NYQL_REVERSE(%s)', ___resolveInP(c))
    }

    @Override
    String cast_to_int(Object col) {
        String.format('CAST(%s AS INT)', ___resolveInP(col))
    }

    @Override
    String cast_to_str(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)

        if (c instanceof List && c.size() > 1) {
            String.format('CAST(%s AS VARCHAR(%s))', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        } else {
            String.format('CAST(%s AS VARCHAR)', ___resolveInP(cx))
        }
    }

    @Override
    String cast_to_date(Object col) {
        String.format('CAST(%s AS DATE)', ___resolveInP(col))
    }

    @Override
    String cast_to_bigint(Object col) {
        String.format('CAST(%s AS BIGINT)', ___resolveInP(col))
    }

    @Override
    String current_timestamp(Object pms) {
        'CURRENT_TIMESTAMP()'
    }

    @Override
    String current_date(Object pms) {
        'CURRENT_DATE()'
    }

    @Override
    String current_time(Object pms) {
        'CURRENT_TIME()'
    }

    @Override
    String current_epoch() {
        'DATEDIFF(\'SECOND\', ' + TIME_EPOCH_START + ', CURRENT_TIMESTAMP()) * 1000'
    }

    @Override
    String epoch_to_date(Object col) {
        String.format('CAST(TIMESTAMPADD(\'SECOND\', %s, ' + TIME_EPOCH_START + ') AS DATE)', ___resolveInP(col))
    }

    @Override
    String epoch_to_datetime(Object col) {
        String.format('TIMESTAMPADD(\'SECOND\', %s, ' + TIME_EPOCH_START + ')', ___resolveInP(col))
    }

    @Override
    String date_diff_years(Object dates) {
        def c = ___val(dates)
        def pmx = ___pm(dates)
        if (c instanceof List) String.format('TIMESTAMPDIFF(\'YEAR\', %s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw requireTwoParams()
    }

    @Override
    String date_diff_months(Object dates) {
        def c = ___val(dates)
        def pmx = ___pm(dates)
        if (c instanceof List) String.format('TIMESTAMPDIFF(\'MONTH\', %s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw requireTwoParams()
    }

    @Override
    String date_diff_days(Object dates) {
        def c = ___val(dates)
        def pmx = ___pm(dates)
        if (c instanceof List) String.format('TIMESTAMPDIFF(\'DAY\', %s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw requireTwoParams()
    }

    @Override
    String date_diff_weeks(Object dates) {
        def c = ___val(dates)
        def pmx = ___pm(dates)
        if (c instanceof List) String.format('TIMESTAMPDIFF(\'WEEK\', %s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw requireTwoParams()
    }

    @Override
    String date_diff_hours(Object dates) {
        def c = ___val(dates)
        def pmx = ___pm(dates)
        if (c instanceof List) String.format('TIMESTAMPDIFF(\'HOUR\', %s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw requireTwoParams()
    }

    @Override
    String date_diff_minutes(Object dates) {
        def c = ___val(dates)
        def pmx = ___pm(dates)
        if (c instanceof List) String.format('TIMESTAMPDIFF(\'MINUTE\', %s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw requireTwoParams()
    }

    @Override
    String date_diff_seconds(Object dates) {
        def c = ___val(dates)
        def pmx = ___pm(dates)
        if (c instanceof List) String.format('TIMESTAMPDIFF(\'SECOND\', %s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        else throw requireTwoParams()
    }

    @Override
    String date_add_days(Object dateBy) {
        def c = ___val(dateBy)
        def pmx = ___pm(dateBy)
        if (c instanceof List) {
            String.format('TIMESTAMPADD(\'DAY\', %s, %s)', ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        } else {
            throw new NySyntaxException('Date add function requires exactly two parameters!')
        }
    }

    @Override
    String date_add_months(Object dateBy) {
        def c = ___val(dateBy)
        def pmx = ___pm(dateBy)
        if (c instanceof List) {
            String.format('TIMESTAMPADD(\'MONTH\', %s, %s)', ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        } else {
            throw new NySyntaxException('Date add function requires exactly two parameters!')
        }
    }

    @Override
    String date_add_years(Object dateBy) {
        def c = ___val(dateBy)
        def pmx = ___pm(dateBy)
        if (c instanceof List) {
            String.format('TIMESTAMPADD(\'YEAR\', %s, %s)', ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        } else {
            throw new NySyntaxException('Date add function requires exactly two parameters!')
        }
    }

    @Override
    String date_add_weeks(Object dateBy) {
        def c = ___val(dateBy)
        def pmx = ___pm(dateBy)
        if (c instanceof List) {
            String.format('TIMESTAMPADD(\'WEEK\', %s, %s)', ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        } else {
            throw new NySyntaxException('Date add function requires exactly two parameters!')
        }
    }

    @Override
    String date_add_hours(Object dateBy) {
        def c = ___val(dateBy)
        def pmx = ___pm(dateBy)
        if (c instanceof List) {
            String.format('TIMESTAMPADD(\'HOUR\', %s, %s)', ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        } else {
            throw new NySyntaxException('Date add function requires exactly two parameters!')
        }
    }

    @Override
    String date_add_minutes(Object dateBy) {
        def c = ___val(dateBy)
        def pmx = ___pm(dateBy)
        if (c instanceof List) {
            String.format('TIMESTAMPADD(\'MINUTE\', %s, %s)', ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        } else {
            throw new NySyntaxException('Date add function requires exactly two parameters!')
        }
    }

    @Override
    String date_add_seconds(Object dateBy) {
        def c = ___val(dateBy)
        def pmx = ___pm(dateBy)
        if (c instanceof List) {
            String.format('TIMESTAMPADD(\'SECOND\', %s, %s)', ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        } else {
            throw new NySyntaxException('Date add function requires exactly two parameters!')
        }
    }

    @Override
    String date_sub_days(Object dateBy) {
        def c = ___val(dateBy)
        def pmx = ___pm(dateBy)
        if (c instanceof List) {
            String.format('TIMESTAMPADD(\'DAY\', -%s, %s)', ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        } else {
            throw new NySyntaxException('Date subtract function requires exactly two parameters!')
        }
    }

    @Override
    String date_sub_months(Object dateBy) {
        def c = ___val(dateBy)
        def pmx = ___pm(dateBy)
        if (c instanceof List) {
            String.format('TIMESTAMPADD(\'MONTH\', -%s, %s)', ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        } else {
            throw new NySyntaxException('Date subtract function requires exactly two parameters!')
        }
    }

    @Override
    String date_sub_years(Object dateBy) {
        def c = ___val(dateBy)
        def pmx = ___pm(dateBy)
        if (c instanceof List) {
            String.format('TIMESTAMPADD(\'YEAR\', -%s, %s)', ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        } else {
            throw new NySyntaxException('Date subtract function requires exactly two parameters!')
        }
    }

    @Override
    String date_sub_weeks(Object dateBy) {
        def c = ___val(dateBy)
        def pmx = ___pm(dateBy)
        if (c instanceof List) {
            String.format('TIMESTAMPADD(\'WEEK\', -%s, %s)', ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        } else {
            throw new NySyntaxException('Date subtract function requires exactly two parameters!')
        }
    }

    @Override
    String date_sub_hours(Object dateBy) {
        def c = ___val(dateBy)
        def pmx = ___pm(dateBy)
        if (c instanceof List) {
            String.format('TIMESTAMPADD(\'HOUR\', -%s, %s)', ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        } else {
            throw new NySyntaxException('Date subtract function requires exactly two parameters!')
        }
    }

    @Override
    String date_sub_minutes(Object dateBy) {
        def c = ___val(dateBy)
        def pmx = ___pm(dateBy)
        if (c instanceof List) {
            String.format('TIMESTAMPADD(\'MINUTE\', -%s, %s)', ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        } else {
            throw new NySyntaxException('Date subtract function requires exactly two parameters!')
        }
    }

    @Override
    String date_sub_seconds(Object dateBy) {
        def c = ___val(dateBy)
        def pmx = ___pm(dateBy)
        if (c instanceof List) {
            String.format('TIMESTAMPADD(\'SECOND\', -%s, %s)', ___resolveIn(c[1], pmx), ___resolveIn(c[0], pmx))
        } else {
            throw new NySyntaxException('Date subtract function requires exactly two parameters!')
        }
    }

    @Override
    String date_trunc(Object it) {
        String.format('CAST(%s AS DATE)', ___resolveInP(it))
    }


    @CompileStatic
    private static NyException requireTwoParams() {
        new NySyntaxException('DATE DIFF function requires exactly two parameters!')
    }

}
