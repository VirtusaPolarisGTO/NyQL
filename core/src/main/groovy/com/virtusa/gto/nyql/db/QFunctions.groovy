package com.virtusa.gto.nyql.db

import com.virtusa.gto.nyql.FunctionColumn
import com.virtusa.gto.nyql.QContextType
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.exceptions.NySyntaxException
import com.virtusa.gto.nyql.model.units.QString
import com.virtusa.gto.nyql.utils.QOperator
import com.virtusa.gto.nyql.utils.QUtils
import groovy.transform.CompileStatic

import java.util.stream.Collectors
/**
 * Contains actual implementation of sql functions.
 *
 * @author IWEERARATHNA
 */
trait QFunctions {

    /**
     * Resolve given object assuming first element is the value and the second
     * value is parameter list.
     *
     * @param obj pair of value and parameter list.
     * @return resolved query string.
     */
    String ___resolveInP(obj) {
        ___resolve(___val(obj), QContextType.INSIDE_FUNCTION, ___pm(obj))
    }

    String ___resolveIn(obj, List paramList) {
        ___resolve(obj, QContextType.INSIDE_FUNCTION, paramList)
    }

    /**
     * Extracts value from function input.
     *
     * @param input function input.
     * @return value
     */
    def ___val(input) {
        input[0]
    }

    /**
     * Extracts parameter list from function input.
     *
     * @param input function input.
     * @return parameter list.
     */
    List ___pm(input) {
        (List) input[1]
    }

    /**
     * COALESCE function.
     *
     * @param input input values.
     * @return function as query string.
     */
    String coalesce(input) {
        def it = ___val(input)
        def pmx = ___pm(input)
        if (it instanceof List) {
            List items = []
            QUtils.expandToList(items, it)
            items.stream().map { op -> return ___resolveIn(op, pmx) }
                    .collect(Collectors.joining(', ', 'COALESCE(', ')'))
        } else {
            'COALESCE(' + ___resolveIn(it, pmx) + ')'
        }
    }

    @CompileStatic abstract String greatest(cx)

    @CompileStatic abstract String least(cx)

    /**
     * ------------------------------------------------------------
     * Date and Time functions
     * ------------------------------------------------------------
     */

    /**
     * Returns current timestamp of system.
     *
     * @return string <i>NOW()</i>
     */
    @CompileStatic String current_timestamp(pms) { 'NOW()' }

    /**
     * Returns current date without time.
     *
     * @return string representation of current date function.
     */
    @CompileStatic String current_date(pms) { 'CURDATE()' }

    /**
     * Returns current time without date.
     *
     * @return string representation of current time function.
     */
    @CompileStatic String current_time(pms) { 'CURTIME()' }

    abstract String current_epoch()
    abstract String epoch_to_date(Object col)
    abstract String epoch_to_datetime(Object col)
    abstract String date_diff_years(Object dates)
    abstract String date_diff_months(Object dates)
    abstract String date_diff_days(Object dates)
    abstract String date_diff_weeks(Object dates)
    abstract String date_diff_hours(Object dates)
    abstract String date_diff_minutes(Object dates)
    abstract String date_diff_seconds(Object dates)

    abstract String date_add_days(Object dateBy)
    abstract String date_add_months(Object dateBy)
    abstract String date_add_years(Object dateBy)
    abstract String date_add_weeks(Object dateBy)
    abstract String date_add_hours(Object dateBy)
    abstract String date_add_minutes(Object dateBy)
    abstract String date_add_seconds(Object dateBy)
    abstract String date_sub_days(Object dateBy)
    abstract String date_sub_months(Object dateBy)
    abstract String date_sub_years(Object dateBy)
    abstract String date_sub_weeks(Object dateBy)
    abstract String date_sub_hours(Object dateBy)
    abstract String date_sub_minutes(Object dateBy)
    abstract String date_sub_seconds(Object dateBy)

    /**
     * Returns date part from datetime or timestamp.
     *
     * @return string representation of converting to date part.
     */
    @CompileStatic abstract String date_trunc(it)

    /**
     * --------------------------------------------------------------
     * String functions.
     * --------------------------------------------------------------
     */

    /**
     * Returns lower case string representation.
     *
     * @param c input column to convert.
     */
    @CompileStatic String lcase(c) {
        String.format('LOWER(%s)', ___resolveInP(c))
    }

    /**
     * Returns upper case string representation.
     *
     * @param c input column to convert.
     */
    @CompileStatic String ucase(c) { String.format('UPPER(%s)', ___resolveInP(c)) }

    /**
     * Returns whitespace trimmed string.
     *
     * @param c input column to convert.
     */
    @CompileStatic String trim(c) { String.format('TRIM(%s)', ___resolveInP(c)) }

    /**
     * Returns the length of string.
     *
     * @param c input column to find length.
     */
    @CompileStatic String len(c) { String.format('CHAR_LENGTH(%s)', ___resolveInP(c)) }

    /**
     * Returns the function of reversed string of given string.
     *
     * @param c input column to find length.
     */
    @CompileStatic String str_reverse(c) { String.format('REVERSE(%s)', ___resolveInP(c)) }

    /**
     * Returns the function of getting first n characters of a string.
     *
     * @param c input column and length as a list.
     */
    @CompileStatic String str_left(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            return String.format('LEFT(%s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        }
        throw new NyException('Insufficient parameters for LEFT function!')
    }

    /**
     * Returns the function of getting first n characters of a string.
     *
     * @param c input column and length as a list.
     */
    @CompileStatic String str_right(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            return String.format('RIGHT(%s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
        }
        throw new NyException('Insufficient parameters for RIGHT function!')
    }

    /**
     * Returns the function of left trimming function.
     *
     * @param c input column to left trim.
     */
    @CompileStatic String str_ltrim(c) { String.format('LTRIM(%s)', ___resolveInP(c)) }

    /**
     * Returns the function of right trimming function.
     *
     * @param c input column to right trim.
     */
    @CompileStatic String str_rtrim(c) { String.format('RTRIM(%s)', ___resolveInP(c)) }

    /**
     * Returns the function of left padding a text.
     *
     * @param c input column, length and character to fill with.
     */
    @CompileStatic String str_lpad(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            if (c.size() == 3) {
                return String.format('LPAD(%s, %s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx), ___resolveIn(c.get(2), pmx))
            } else if (c.size() == 2) {
                return String.format('LPAD(%s, %s, \" \")', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
            }
        }
        throw new NyException('Insufficient parameters for left pad function!')
    }

    /**
     * Returns the function of right padding a text.
     *
     * @param c input column, length and character to fill with.
     */
    @CompileStatic String str_rpad(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            if (c.size() == 3) {
                return String.format('RPAD(%s, %s, %s)', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx), ___resolveIn(c.get(2), pmx))
            } else if (c.size() == 2) {
                return String.format('RPAD(%s, %s, \" \")', ___resolveIn(c.get(0), pmx), ___resolveIn(c.get(1), pmx))
            }
        }
        throw new NyException('Insufficient parameters for right pad function!')
    }

    /**
     * Returns string replacement function.
     *
     * @param c inputs required for string.
     * @return string representation of replace function.
     */
    @CompileStatic abstract String str_replace(c)

    /**
     * Returns string repeat function.
     *
     * @param c inputs required for string.
     * @return string representation of repeat function.
     */
    @CompileStatic abstract String str_repeat(c)

    /**
     * Returns substring of the given string starting from start position and length.
     *
     * @param c input column to substring.
     */
    @CompileStatic abstract String substr(c)

    /**
     * Returns position of string in the given main string.
     *
     * @param c input column to find position.
     */
    @CompileStatic abstract String position(c)

    /**
     * Returns last position of string in the given main string.
     *
     * @param c input column to find position.
     */
    @CompileStatic abstract String position_last(c)

    /**
     * --------------------------------------------------------------
     * Math functions.
     * --------------------------------------------------------------
     */

    @CompileStatic String trig_sin(cx) {
        String.format('SIN(%s)', ___resolveInP(cx))
    }

    @CompileStatic String trig_cos(cx) {
        String.format('COS(%s)', ___resolveInP(cx))
    }

    @CompileStatic String trig_tan(cx) {
        String.format('TAN(%s)', ___resolveInP(cx))
    }

    @CompileStatic String trig_cot(cx) {
        String.format('COT(%s)', ___resolveInP(cx))
    }

    @CompileStatic String trig_acos(cx) {
        String.format('ACOS(%s)', ___resolveInP(cx))
    }

    @CompileStatic String trig_asin(cx) {
        String.format('ASIN(%s)', ___resolveInP(cx))
    }

    @CompileStatic String trig_atan(cx) {
        String.format('ATAN(%s)', ___resolveInP(cx))
    }

    @CompileStatic String trig_atan2(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('ATAN2(%s, %s)', ___resolveIn(((List)c)[0], pmx), ___resolveIn(((List)c)[1], pmx))
        else throw new NyException('ATAN2 function requires two parameters!')
    }

    @CompileStatic String lg_exp(cx) {
        String.format('EXP(%s)', ___resolveInP(cx))
    }

    @CompileStatic String lg_ln(cx) {
        String.format('LN(%s)', ___resolveInP(cx))
    }

    @CompileStatic String lg_log(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('LOG(%s, %s)', ___resolveIn(((List)c)[0], pmx), ___resolveIn(((List)c)[1], pmx))
        else throw new NyException('LOG function requires two parameters!')
    }

    @CompileStatic abstract String truncate(c)

    /**
     * Rounds a column value to given number of decimal digits.
     *
     * @param c input column and the number of decimal digits.
     * @return String representation of round function.
     */
    @CompileStatic String round(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) String.format('ROUND(%s, %s)', ___resolveIn(((List)c)[0], pmx), ___resolveIn(((List)c)[1], pmx))
        else throw new NyException('ROUND function requires two parameters!')
    }

    /**
     * Returns floor value of the column.
     *
     * @param c input column.
     */
    @CompileStatic String floor(c) { String.format('FLOOR(%s)', ___resolveInP(c)) }

    /**
     * Returns ceiling value of the column.
     *
     * @param c input column.
     */
    @CompileStatic String ceil(c) { String.format('CEILING(%s)', ___resolveInP(c)) }

    /**
     * Returns absolute value of the column.
     *
     * @param c input column.
     */
    @CompileStatic String abs(c) { String.format('ABS(%s)', ___resolveInP(c)) }

    /**
     * Returns a number raised to a power. <b>x ^ y</b>
     *
     * @param c input base and power value.
     * @return String representation of power value.
     */
    @CompileStatic String power(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            String.format('POWER(%s, %s)', ___resolveIn(((List)c)[0], pmx), ___resolveIn(((List)c)[1], pmx))
        } else {
            throw new NySyntaxException('Power requires exactly two parameters!')
        }
    }

    /**
     * Returns the square root of a non-negative object.
     *
     * @param c input column
     * @return String representation of sqrt function.
     */
    @CompileStatic String sqrt(c) {
        String.format('SQRT(%s)', ___resolveInP(c))
    }

    /**
     * Returns the sign of the value +1, 0, -1.
     *
     * @param c input column
     * @return String representation of sign function.
     */
    @CompileStatic String num_sign(c) {
        String.format('SIGN(%s)', ___resolveInP(c))
    }

    /**
     * Returns conversion of radians value to degrees.
     *
     * @param c input column
     * @return String representation of degrees function.
     */
    @CompileStatic String num_degrees(c) {
        String.format('DEGREES(%s)', ___resolveInP(c))
    }

    /**
     * Returns conversion of degrees value to radians.
     *
     * @param c input column
     * @return String representation of radians function.
     */
    @CompileStatic String num_radians(c) {
        String.format('RADIANS(%s)', ___resolveInP(c))
    }

    /**
     * Distinct function to use unique columns.
     *
     * @param c input columns.
     * @return String representation of distinct.
     */
    @CompileStatic String distinct(c) { String.format('DISTINCT(%s)', ___resolveInP(c)) }

    /**
     * Check a value is in between given two values. Requires minimum two values.
     *
     * @param c input values
     * @return string representation of between.
     */
    @CompileStatic String between(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            String.format('BETWEEN %s AND %s', ___resolveIn(((List)c)[0], pmx), ___resolveIn(((List)c)[1], pmx))
        } else {
            throw new NyException('Invalid syntax for BETWEEN function!')
        }
    }

    /**
     * Check a value is not in between given two values. Requires minimum two values.
     *
     * @param c input values
     * @return string representation of not between.
     */
    @CompileStatic String not_between(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof List) {
            String.format('NOT BETWEEN %s AND %s', ___resolveIn(((List)c)[0], pmx), ___resolveIn(((List)c)[1], pmx))
        } else {
            throw new NyException('Invalid syntax for BETWEEN function!')
        }
    }

    /**
     * Two strings check for their likeliness using pattern matching.
     *
     * @param c input column.
     * @return string representation of like.
     */
    @CompileStatic String like(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        String.format('LIKE %s', (c instanceof List) ? ___resolveIn(((List)c)[0], pmx) : ___resolveIn(c, pmx))
    }

    /**
     * Two strings check for their nor likeliness.
     *
     * @param c input column.
     * @return string representation of not like.
     */
    @CompileStatic String not_like(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        String.format('NOT LIKE %s', (c instanceof List) ? ___resolveIn(((List)c)[0], pmx) : ___resolveIn(c, pmx))
    }

    /**
     * Concatenate a set of objects/strings.
     *
     * @param c input objects or columns to concatenate.
     * @return string representation of concatenation.
     */
    String concat(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof String) {
            return String.valueOf(c)
        } else {
            List list
            if (c instanceof FunctionColumn) {
                list = (List)c._columns
            } else if (c instanceof List) {
                list = (List)c
            } else {
                return null
            }
            return list.stream().map {
                col -> if (col instanceof String) {
                    return String.valueOf(col)
                } else {
                    return (String)___resolveIn(col, pmx)
                }
            }.collect(Collectors.joining(', ', 'CONCAT(', ')'))
        }
    }

    /**
     * Concatenate a set of objects/strings converting null values to empties.
     *
     * @param c input objects or columns to concatenate.
     * @return string representation of concatenation.
     */
    String concat_nn(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof String) {
            return String.valueOf(c)
        } else {
            List list
            if (c instanceof FunctionColumn) {
                list = (List)c._columns
            } else if (c instanceof List) {
                list = (List)c
            } else {
                return null
            }
            QString emptyStr = new QString()
            emptyStr.text = ""

            return list.stream()
                .filter({it -> it != null})
                .map {
                    col -> if (col instanceof String) {
                        return String.valueOf(col)
                    } else if (col instanceof QString) {
                        return ___resolveIn(col, pmx)
                    } else {
                        def ip = [[___resolveIn(col, pmx), emptyStr], pmx]
                        return coalesce(ip)
                    }
            }.collect(Collectors.joining(', ', 'CONCAT(', ')'))
        }
    }

    /**
     * Concatenate a set of objects/strings with given separator.
     *
     * @param c input objects or columns to concatenate.
     * @return string representation of concatenation.
     */
    String concat_ws(cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)
        if (c instanceof String) {
            return String.valueOf(c)
        } else {
            List list
            Object sep = null
            if (c instanceof FunctionColumn) {
                list = (List)c._columns
            } else if (c instanceof List) {
                list = (List)c.get(1)
                sep = c.get(0)
            } else {
                return null
            }

            String pfx = 'CONCAT_WS(' + ___resolveIn(sep, pmx) + ', '
            return list.stream().map {
                col -> if (col instanceof String) {
                    return String.valueOf(col)
                } else {
                    return (String)___resolveIn(col, pmx)
                }
            }.collect(Collectors.joining(', ', pfx, ')'))
        }
    }

    /**
     * Cast the given column to integer value.
     *
     * @param col input column.
     * @return string representation of cast to int.
     */
    @CompileStatic abstract String cast_to_int(Object col)

    /**
     * Cast the given column to big integer value.
     *
     * @param col input column.
     * @return string representation of cast to bigint.
     */
    @CompileStatic abstract String cast_to_bigint(Object col)

    /**
     * Cast the given column to string value.
     *
     * @param col input column.
     * @return string representation of cast to string.
     */
    @CompileStatic abstract String cast_to_str(Object col)

    /**
     * Cast the given column to date value.
     *
     * @param col input column.
     * @return string representation of cast to date.
     */
    @CompileStatic abstract String cast_to_date(Object col)

    /**
     * Returns the ascending representation of a column.
     *
     * @param c input column.
     * @return string representation of ascending.
     */
    String asc(c) {
        return ___resolve(___val(c), QContextType.ORDER_BY, ___pm(c)) + ' ASC'
    }

    /**
     * Returns the descending representation of a column.
     *
     * @param c input column.
     * @return string representation of descending.
     */
    String desc(c) {
        return ___resolve(___val(c), QContextType.ORDER_BY, ___pm(c)) + ' DESC'
    }

    /**
     * Returns the count (number of occurrences) value of given column.
     *
     * @param c input column.
     * @return string representation of function
     */
    @CompileStatic String count(c) {
        String.format('COUNT(%s)', ___resolveIn(___val(c) ?: '*', ___pm(c)))
    }

    /**
     * --------------------------------------------------------------
     * Stat functions
     * --------------------------------------------------------------
     */

    @CompileStatic String stat_stddevpop(cx) {
        String.format('STDDEV_POP(%s)', ___resolveInP(cx))
    }

    @CompileStatic String stat_stddevsamp(cx) {
        String.format('STDDEV_SAMP(%s)', ___resolveInP(cx))
    }

    @CompileStatic String stat_varpop(cx) {
        String.format('VAR_POP(%s)', ___resolveInP(cx))
    }

    @CompileStatic String stat_varsamp(cx) {
        String.format('VAR_SAMP(%s)', ___resolveInP(cx))
    }

    /**
     * Returns the sum value of given column.
     *
     * @param c input column.
     * @return string representation of function
     */
    @CompileStatic String sum(c) {
        String.format('SUM(%s)', ___resolveIn(___val(c) ?: '*', ___pm(c)))
    }

    /**
     * Returns the average value of given column.
     *
     * @param c input column.
     * @return string representation of function
     */
    @CompileStatic String avg(c) {
        String.format('AVG(%s)', ___resolveIn(___val(c) ?: '*', ___pm(c)))
    }

    /**
     * Returns the min value of given column.
     *
     * @param c input column.
     * @return string representation of function
     */
    @CompileStatic String min(c) {
        String.format('MIN(%s)', ___resolveIn(___val(c) ?: '*', ___pm(c)))
    }

    /**
     * Returns the max value of given column.
     *
     * @param c input column.
     * @return string representation of function
     */
    @CompileStatic String max(c) {
        String.format('MAX(%s)', ___resolveIn(___val(c) ?: '*', ___pm(c)))
    }

    /**
     * Addition operator; <b>a + b</b>
     * <br/>
     * Accepts minimum of two parameters and maximum is unbounded.
     *
     * @param it function input parameters.
     * @return string representation of function.
     */
    String op_add(itx) {
        def it = ___val(itx)
        def pmx = ___pm(itx)
        if (it instanceof List) {
            List items = []
            QUtils.expandToList(items, it)
            items.stream().map { op -> return ___resolveIn(op, pmx) }
                    .collect(Collectors.joining(' + ', '(', ')'))
        } else {
            throw new NySyntaxException('Add operation requires at least two operands!')
        }
    }

    /**
     * Subtraction operator; <b>a - b</b>
     *
     * @param it function input parameters.
     * @return string representation of function.
     */
    @CompileStatic String op_minus(itx) {
        def it = ___val(itx)
        def pmx = ___pm(itx)
        if (it instanceof List) {
            String.format('(%s - %s)', ___resolveIn(((List)it)[0], pmx), ___resolveIn(((List)it)[1], pmx))
        } else {
            throw new NySyntaxException('Minus operation requires at least two operands!')
        }
    }

    /**
     * Multiplication operator; <b>a * b</b>
     *
     * @param it function input parameters.
     * @return string representation of function.
     */
    @CompileStatic String op_multiply(itx) {
        def it = ___val(itx)
        def pmx = ___pm(itx)
        if (it instanceof List) {
            String.format('(%s * %s)', ___resolveIn(((List)it)[0], pmx), ___resolveIn(((List)it)[1], pmx))
        } else {
            throw new NySyntaxException('Multiply operation requires at least two operands!')
        }
    }

    /**
     * Division operator; <b>a / b</b>
     *
     * @param it function input parameters.
     * @return string representation of function.
     */
    @CompileStatic String op_divide(itx) {
        def it = ___val(itx)
        def pmx = ___pm(itx)
        if (it instanceof List) {
            String.format('(%s / %s)', ___resolveIn(((List)it)[0], pmx), ___resolveIn(((List)it)[1], pmx))
        } else {
            throw new NySyntaxException('Divide operation requires at least two operands!')
        }
    }

    /**
     * Modulus operator; <b>a % b</b>
     *
     * @param it function input parameters.
     * @return string representation of function.
     */
    @CompileStatic String op_modulus(itx) {
        def it = ___val(itx)
        def pmx = ___pm(itx)
        if (it instanceof List) {
            String.format('(%s %% %s)', ___resolveIn(((List)it)[0], pmx), ___resolveIn(((List)it)[1], pmx))
        } else {
            throw new NySyntaxException('Modulus operation requires at least two operands!')
        }
    }

    /**
     * Bitwise And operation.
     *
     * @param it function input parameters.
     * @return string representation of function.
     */
    @CompileStatic String op_bit_and(itx) {
        def it = ___val(itx)
        def pmx = ___pm(itx)
        if (it instanceof List) {
            String.format('%s & %s', ___resolveIn(((List)it)[0], pmx), ___resolveIn(((List)it)[1], pmx))
        } else {
            throw new NySyntaxException('Bitwise And operation requires at least two operands!')
        }
    }

    /**
     * Bitwise Or operation.
     *
     * @param it function input parameters.
     * @return string representation of function.
     */
    @CompileStatic String op_bit_or(itx) {
        def it = ___val(itx)
        def pmx = ___pm(itx)
        if (it instanceof List) {
            String.format('%s | %s', ___resolveIn(((List)it)[0], pmx), ___resolveIn(((List)it)[1], pmx))
        } else {
            throw new NySyntaxException('Bitwise Or operation requires at least two operands!')
        }
    }

    /**
     * Bitwise Or operation.
     *
     * @param it function input parameters.
     * @return string representation of function.
     */
    @CompileStatic String op_bit_xor(itx) {
        def it = ___val(itx)
        def pmx = ___pm(itx)
        if (it instanceof List) {
            String.format('%s ^ %s', ___resolveIn(((List)it)[0], pmx), ___resolveIn(((List)it)[1], pmx))
        } else {
            throw new NySyntaxException('Bitwise Xor operation requires at least two operands!')
        }
    }

    /**
     * Bitwise Not operation.
     *
     * @param it function input parameters.
     * @return string representation of function.
     */
    @CompileStatic String op_bit_not(itx) {
        def it = ___val(itx)
        def pmx = ___pm(itx)
        String.format('~%s', (it instanceof List) ? ___resolveIn(((List)it)[0], pmx) : ___resolveIn(it, pmx))
    }

    /**
     * Exists function for projection.
     *
     * @param it inner query.
     * @return generated function.
     */
    String exists(itx) {
        def it = ___val(itx)
        QOperator.EXISTS.getOp() + (String) ___resolve(it, QContextType.UNKNOWN, ___pm(itx))
    }
}