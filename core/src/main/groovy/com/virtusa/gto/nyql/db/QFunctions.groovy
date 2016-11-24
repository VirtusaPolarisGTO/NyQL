package com.virtusa.gto.nyql.db

import com.virtusa.gto.nyql.FunctionColumn
import com.virtusa.gto.nyql.QContextType
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.exceptions.NySyntaxException
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

    String ___resolveIn(def obj) {
        ___resolve(obj, QContextType.INSIDE_FUNCTION)
    }

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
    @CompileStatic String current_timestamp() { 'NOW()' }

    /**
     * Returns current date without time.
     *
     * @return string representation of current date function.
     */
    @CompileStatic String current_date() { 'CURDATE()' }

    /**
     * Returns current time without date.
     *
     * @return string representation of current time function.
     */
    @CompileStatic String current_time() { 'CURTIME()' }

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
    @CompileStatic String lcase(c) { String.format('LOWER(%s)', ___resolveIn(c)) }

    /**
     * Returns upper case string representation.
     *
     * @param c input column to convert.
     */
    @CompileStatic String ucase(c) { String.format('UPPER(%s)', ___resolveIn(c)) }

    /**
     * Returns whitespace trimmed string.
     *
     * @param c input column to convert.
     */
    @CompileStatic String trim(c) { String.format('TRIM(%s)', ___resolveIn(c)) }

    /**
     * Returns the length of string.
     *
     * @param c input column to find length.
     */
    @CompileStatic String len(c) { String.format('CHAR_LENGTH(%s)', ___resolveIn(c)) }

    /**
     * Returns the function of reversed string of given string.
     *
     * @param c input column to find length.
     */
    @CompileStatic String str_reverse(c) { String.format('REVERSE(%s)', ___resolveIn(c)) }

    /**
     * Returns the function of getting first n characters of a string.
     *
     * @param c input column and length as a list.
     */
    @CompileStatic String str_left(c) {
        if (c instanceof List) {
            return String.format('LEFT(%s, %s)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        }
        throw new NyException('Insufficient parameters for LEFT function!')
    }

    /**
     * Returns the function of getting first n characters of a string.
     *
     * @param c input column and length as a list.
     */
    @CompileStatic String str_right(c) {
        if (c instanceof List) {
            return String.format('RIGHT(%s, %s)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
        }
        throw new NyException('Insufficient parameters for RIGHT function!')
    }

    /**
     * Returns the function of left trimming function.
     *
     * @param c input column to left trim.
     */
    @CompileStatic String str_ltrim(c) { String.format('LTRIM(%s)', ___resolveIn(c)) }

    /**
     * Returns the function of right trimming function.
     *
     * @param c input column to right trim.
     */
    @CompileStatic String str_rtrim(c) { String.format('RTRIM(%s)', ___resolveIn(c)) }

    /**
     * Returns the function of left padding a text.
     *
     * @param c input column, length and character to fill with.
     */
    @CompileStatic String str_lpad(c) {
        if (c instanceof List) {
            if (c.size() == 3) {
                return String.format('LPAD(%s, %s, %s)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)), ___resolveIn(c.get(2)))
            } else if (c.size() == 2) {
                return String.format('LPAD(%s, %s, \" \")', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
            }
        }
        throw new NyException('Insufficient parameters for left pad function!')
    }

    /**
     * Returns the function of right padding a text.
     *
     * @param c input column, length and character to fill with.
     */
    @CompileStatic String str_rpad(c) {
        if (c instanceof List) {
            if (c.size() == 3) {
                return String.format('RPAD(%s, %s, %s)', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)), ___resolveIn(c.get(2)))
            } else if (c.size() == 2) {
                return String.format('RPAD(%s, %s, \" \")', ___resolveIn(c.get(0)), ___resolveIn(c.get(1)))
            }
        }
        throw new NyException('Insufficient parameters for right pad function!')
    }

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
     * Math functions.
     */

    /**
     * Rounds a column value to given number of decimal digits.
     *
     * @param c input column and the number of decimal digits.
     * @return String representation of round function.
     */
    @CompileStatic String round(c) {
        if (c instanceof List) String.format('ROUND(%s, %s)', ___resolveIn(((List)c)[0]), ___resolveIn(((List)c)[1]))
        else throw new NyException('ROUND function requires two parameters!')
    }

    /**
     * Returns floor value of the column.
     *
     * @param c input column.
     */
    @CompileStatic String floor(c) { String.format('FLOOR(%s)', ___resolveIn(c)) }

    /**
     * Returns ceiling value of the column.
     *
     * @param c input column.
     */
    @CompileStatic String ceil(c) { String.format('CEILING(%s)', ___resolveIn(c)) }

    /**
     * Returns absolute value of the column.
     *
     * @param c input column.
     */
    @CompileStatic String abs(c) { String.format('ABS(%s)', ___resolveIn(c)) }

    /**
     * Returns a number raised to a power. <b>x ^ y</b>
     *
     * @param c input base and power value.
     * @return String representation of power value.
     */
    @CompileStatic String power(c) {
        if (c instanceof List) {
            String.format('POWER(%s, %s)', ___resolveIn(((List)c)[0]), ___resolveIn(((List)c)[1]))
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
        String.format('SQRT(%s)', ___resolveIn(c))
    }

    /**
     * Returns the sign of the value +1, 0, -1.
     *
     * @param c input column
     * @return String representation of sign function.
     */
    @CompileStatic String num_sign(c) {
        String.format('SIGN(%s)', ___resolveIn(c))
    }

    /**
     * Returns conversion of radians value to degrees.
     *
     * @param c input column
     * @return String representation of degrees function.
     */
    @CompileStatic String num_degrees(c) {
        String.format('DEGREES(%s)', ___resolveIn(c))
    }

    /**
     * Returns conversion of degrees value to radians.
     *
     * @param c input column
     * @return String representation of radians function.
     */
    @CompileStatic String num_radians(c) {
        String.format('RADIANS(%s)', ___resolveIn(c))
    }

    /**
     * Distinct function to use unique columns.
     *
     * @param c input columns.
     * @return String representation of distinct.
     */
    @CompileStatic String distinct(c) { String.format('DISTINCT(%s)', ___resolveIn(c)) }

    /**
     * Check a value is in between given two values. Requires minimum two values.
     *
     * @param c input values
     * @return string representation of between.
     */
    @CompileStatic String between(c) {
        if (c instanceof List) {
            String.format('BETWEEN %s AND %s', ___resolveIn(((List)c)[0]), ___resolveIn(((List)c)[1]))
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
    @CompileStatic String not_between(c) {
        if (c instanceof List) {
            String.format('NOT BETWEEN %s AND %s', ___resolveIn(((List)c)[0]), ___resolveIn(((List)c)[1]))
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
    @CompileStatic String like(c) {
        String.format('LIKE %s', (c instanceof List) ? ___resolveIn(((List)c)[0]) : ___resolveIn(c))
    }

    /**
     * Two strings check for their nor likeliness.
     *
     * @param c input column.
     * @return string representation of not like.
     */
    @CompileStatic String not_like(c) {
        String.format('NOT LIKE %s', (c instanceof List) ? ___resolveIn(((List)c)[0]) : ___resolveIn(c))
    }

    /**
     * Concatenate a set of objects/strings.
     *
     * @param c input objects or columns to concatenate.
     * @return string representation of concatenation.
     */
    String concat(c) {
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
                    return (String)___resolveIn(col)
                }
            }.collect(Collectors.joining(', ', 'CONCAT(', ')'))
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
        return ___resolve(c, QContextType.ORDER_BY) + ' ASC'
    }

    /**
     * Returns the descending representation of a column.
     *
     * @param c input column.
     * @return string representation of descending.
     */
    String desc(c) {
        return ___resolve(c, QContextType.ORDER_BY) + ' DESC'
    }

    /**
     * Returns the count (number of occurrences) value of given column.
     *
     * @param c input column.
     * @return string representation of function
     */
    @CompileStatic String count(c) {
        String.format('COUNT(%s)', ___resolveIn(c ?: '*'))
    }

    /**
     * Returns the sum value of given column.
     *
     * @param c input column.
     * @return string representation of function
     */
    @CompileStatic String sum(c) {
        String.format('SUM(%s)', ___resolveIn(c ?: '*'))
    }

    /**
     * Returns the average value of given column.
     *
     * @param c input column.
     * @return string representation of function
     */
    @CompileStatic String avg(c) {
        String.format('AVG(%s)', ___resolveIn(c ?: '*'))
    }

    /**
     * Returns the min value of given column.
     *
     * @param c input column.
     * @return string representation of function
     */
    @CompileStatic String min(c) {
        String.format('MIN(%s)', ___resolveIn(c ?: '*'))
    }

    /**
     * Returns the max value of given column.
     *
     * @param c input column.
     * @return string representation of function
     */
    @CompileStatic String max(c) {
        String.format('MAX(%s)', ___resolveIn(c ?: '*'))
    }

    /**
     * Addition operator; <b>a + b</b>
     * <br/>
     * Accepts minimum of two parameters and maximum is unbounded.
     *
     * @param it function input parameters.
     * @return string representation of function.
     */
    String op_add(it) {
        if (it instanceof List) {
            List items = []
            QUtils.expandToList(items, it)
            items.stream().map { op -> return ___resolveIn(op) }
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
    @CompileStatic String op_minus(it) {
        if (it instanceof List) {
            String.format('(%s - %s)', ___resolveIn(((List)it)[0]), ___resolveIn(((List)it)[1]))
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
    @CompileStatic String op_multiply(it) {
        if (it instanceof List) {
            String.format('(%s * %s)', ___resolveIn(((List)it)[0]), ___resolveIn(((List)it)[1]))
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
    @CompileStatic String op_divide(it) {
        if (it instanceof List) {
            String.format('(%s / %s)', ___resolveIn(((List)it)[0]), ___resolveIn(((List)it)[1]))
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
    @CompileStatic String op_modulus(it) {
        if (it instanceof List) {
            String.format('(%s %% %s)', ___resolveIn(((List)it)[0]), ___resolveIn(((List)it)[1]))
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
    @CompileStatic String op_bit_and(it) {
        if (it instanceof List) {
            String.format('%s & %s', ___resolveIn(((List)it)[0]), ___resolveIn(((List)it)[1]))
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
    @CompileStatic String op_bit_or(it) {
        if (it instanceof List) {
            String.format('%s | %s', ___resolveIn(((List)it)[0]), ___resolveIn(((List)it)[1]))
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
    @CompileStatic String op_bit_xor(it) {
        if (it instanceof List) {
            String.format('%s ^ %s', ___resolveIn(((List)it)[0]), ___resolveIn(((List)it)[1]))
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
    @CompileStatic String op_bit_not(it) {
        String.format('~%s', (it instanceof List) ? ___resolveIn(((List)it)[0]) : ___resolveIn(it))
    }

    /**
     * Exists function for projection.
     *
     * @param it inner query.
     * @return generated function.
     */
    String exists(it) {
        QOperator.EXISTS.getOp() + (String)___resolve(it, QContextType.UNKNOWN)
    }
}