package com.virtusa.gto.insight.nyql.db

import com.virtusa.gto.insight.nyql.FunctionColumn
import com.virtusa.gto.insight.nyql.QContextType
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.exceptions.NySyntaxException
import com.virtusa.gto.insight.nyql.utils.QOperator
import com.virtusa.gto.insight.nyql.utils.QUtils
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
    @CompileStatic String lcase(c) { 'LOWER(' + ___resolveIn(c) + ')' }

    /**
     * Returns upper case string representation.
     *
     * @param c input column to convert.
     */
    @CompileStatic String ucase(c) { 'UPPER(' + ___resolveIn(c) + ')' }

    /**
     * Returns whitespace trimmed string.
     *
     * @param c input column to convert.
     */
    @CompileStatic String trim(c) { 'TRIM(' + ___resolveIn(c) + ')' }

    /**
     * Returns the length of string.
     *
     * @param c input column to find length.
     */
    @CompileStatic String len(c) { 'CHAR_LENGTH(' + ___resolveIn(c) + ')' }

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
        if (c instanceof List) 'ROUND(' + ___resolveIn(((List)c)[0]) + ', ' + ___resolveIn(((List)c)[1]) + ')'
        else throw new NyException('ROUND function requires two parameters!')
    }

    /**
     * Returns floor value of the column.
     *
     * @param c input column.
     */
    @CompileStatic String floor(c) { 'FLOOR(' + ___resolveIn(c) + ')' }

    /**
     * Returns ceiling value of the column.
     *
     * @param c input column.
     */
    @CompileStatic String ceil(c) { 'CEILING(' + ___resolveIn(c) + ')' }

    /**
     * Returns absolute value of the column.
     *
     * @param c input column.
     */
    @CompileStatic String abs(c) { 'ABS(' + ___resolveIn(c) + ')' }

    /**
     * Returns a number raised to a power. <b>x ^ y</b>
     *
     * @param c input base and power value.
     * @return String representation of power value.
     */
    @CompileStatic String power(c) {
        if (c instanceof List) {
            'POWER(' + ___resolveIn(((List)c)[0]) + ', ' + ___resolveIn(((List)c)[1]) + ')'
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
        'SQRT(' + ___resolveIn(c) + ")"
    }

    /**
     * Distinct function to use unique columns.
     *
     * @param c input columns.
     * @return String representation of distinct.
     */
    @CompileStatic String distinct(c) { 'DISTINCT(' + ___resolveIn(c) + ')' }

    /**
     * Check a value is in between given two values. Requires minimum two values.
     *
     * @param c input values
     * @return string representation of between.
     */
    @CompileStatic String between(c) {
        if (c instanceof List) {
            return 'BETWEEN ' + ___resolveIn(((List)c)[0]) + ' AND ' + ___resolveIn(((List)c)[1])
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
            return 'NOT BETWEEN ' + ___resolveIn(((List)c)[0]) + ' AND ' + ___resolveIn(((List)c)[1])
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
        if (c instanceof List) { return 'LIKE ' + ___resolveIn(((List)c)[0]) }
        else { return 'LIKE ' + ___resolveIn(c) }
    }

    /**
     * Two strings check for their nor likeliness.
     *
     * @param c input column.
     * @return string representation of not like.
     */
    @CompileStatic String not_like(c) {
        if (c instanceof List) { return 'NOT LIKE ' + ___resolveIn(((List)c)[0]) }
        else { return 'NOT LIKE ' + ___resolveIn(c) }
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
        'COUNT(' + ___resolveIn(c ?: '*') + ')'
    }

    /**
     * Returns the distinct count value of given column.
     *
     * @param c input column.
     * @return string representation of function
     */
    @CompileStatic String count_distinct(c) {
        c == null ? 'COUNT(DISTINCT)' : 'COUNT(' + distinct(c) + ')'
    }

    /**
     * Returns the sum value of given column.
     *
     * @param c input column.
     * @return string representation of function
     */
    @CompileStatic String sum(c) {
        'SUM(' + ___resolveIn(c ?: '*') + ')'
    }

    /**
     * Returns the average value of given column.
     *
     * @param c input column.
     * @return string representation of function
     */
    @CompileStatic String avg(c) {
        'AVG(' + ___resolveIn(c ?: '*') + ')'
    }

    /**
     * Returns the min value of given column.
     *
     * @param c input column.
     * @return string representation of function
     */
    @CompileStatic String min(c) {
        'MIN(' + ___resolveIn(c ?: '*') + ')'
    }

    /**
     * Returns the max value of given column.
     *
     * @param c input column.
     * @return string representation of function
     */
    @CompileStatic String max(c) {
        'MAX(' + ___resolveIn(c ?: '*') + ')'
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
            return '(' + ___resolveIn(((List)it)[0]) + ' - ' + ___resolveIn(((List)it)[1]) + ')'
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
            return '(' + ___resolveIn(((List)it)[0]) + ' * ' + ___resolveIn(((List)it)[1]) + ')'
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
            return '(' + ___resolveIn(((List)it)[0]) + ' / ' + ___resolveIn(((List)it)[1]) + ')'
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
            return '(' + ___resolveIn(((List)it)[0]) + ' % ' + ___resolveIn(((List)it)[1]) + ')'
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
            return ___resolveIn(((List)it)[0]) + ' & ' + ___resolveIn(((List)it)[1])
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
            return ___resolveIn(((List)it)[0]) + ' | ' + ___resolveIn(((List)it)[1])
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
            return ___resolveIn(((List)it)[0]) + ' ^ ' + ___resolveIn(((List)it)[1])
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
        if (it instanceof List) {
            return '~' + ___resolveIn(((List)it)[0])
        } else {
            return '~' + ___resolveIn(it)
        }
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