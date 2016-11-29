package com.virtusa.gto.nyql.engine.impl

import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.utils.QUtils
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
/**
 * Instance containing jdbc results after an execution of a query.
 *
 * @author IWEERARATHNA
 */
class NyQLResult extends LinkedList<Map<String, Object>> {

    private static final int FIRST = 0

    private static final String TRUE = 'true'
    private static final String PG_T = 't'
    private static final String YES = 'yes'

    @CompileStatic
    long affectedCount() {
        if (!isEmpty()) {
            Map<String, Object> record = get(FIRST)
            if (record != null && record.containsKey(JDBCConstants.COUNT_KEY)) {
                return (long) record.get(JDBCConstants.COUNT_KEY)
            }
        }
        throw new NyException(QUtils.generateErrStr('The result list does not contain a valid count result!',
            'May be you are not executing a insert/update statement?'))
    }

    @CompileStatic
    List<?> affectedKeys() {
        if (!isEmpty()) {
            Map<String, Object> record = get(FIRST)
            if (record != null && record.containsKey(JDBCConstants.KEYS_KEY)) {
                return (List) record.get(JDBCConstants.KEYS_KEY)
            }
        }
        throw new NyException(QUtils.generateErrStr('The result list does not contain a return keys in the result!',
                'May be you are not executing a insert/update statement? (Not supported for bulk insert/update in JDBC yet)',
                'Or, you are not specifying explicitly to return keys from the statement',
                'Or, even JDBC driver might not returning it for you. Consider a different strategy instead.'))
    }

    /**
     * In-place convert all column values to a boolean column, so user
     * does not need to convert it again and again for each individual
     * values.
     *
     * @param column column to convert to bool.
     * @return this mutated instance.
     */
    @CompileStatic
    NyQLResult mutateToBool(String column) {
        if (!isEmpty()) {
            for (Map<String, Object> row : this) {
                row.put(column, toBool(row.get(column)))
            }
        }
        this
    }

    /**
     * In-place convert all values in the given column to a integer column, so user
     * does not need to convert it again and again for each individual
     * values.
     *
     * @param column column to convert to integers.
     * @return this mutated instance.
     */
    @CompileStatic
    NyQLResult mutateToInt(String column) {
        if (!isEmpty()) {
            for (Map<String, Object> row : this) {
                row.put(column, toInt(row.get(column)))
            }
        }
        this
    }

    /**
     * In-place convert all values in the given column to a double values, so user
     * does not need to convert it again and again for each individual
     * values.
     *
     * @param column column to convert to integers.
     * @return this mutated instance.
     */
    @CompileStatic
    NyQLResult mutateToDouble(String column) {
        if (!isEmpty()) {
            for (Map<String, Object> row : this) {
                row.put(column, toDouble(row.get(column)))
            }
        }
        this
    }

    /**
     * Returns the column value as a boolean. If value is NULL, then return value will be null
     * as well.
     *
     * @param index record index. This must be between 0 to N-1 inclusively.
     * @param column column name.
     * @return value as boolean.
     */
    @CompileStatic
    Boolean asBool(int index, String column) {
        Map<String, Object> record = get(index)
        if (record.containsKey(column)) {
            toBool(record.get(column))
        } else {
            throw new NyException("The requested column '$column' does not exist in the specified record index at '$index'!")
        }
    }

    @CompileStatic
    private static toBool(Object val) {
        if (val == null) {
            null
        } else if (val instanceof Number) {
            ((Number) val).compareTo(0) != 0
        } else {
            String tx = String.valueOf(val)
            TRUE.equalsIgnoreCase(tx) || PG_T.equalsIgnoreCase(tx) || YES.equalsIgnoreCase(tx)
        }
    }

    @CompileStatic
    private static toInt(Object val) {
        if (val == null) {
            null
        } else if (val instanceof Number) {
            ((Number) val).intValue()
        } else {
            Integer.parseInt(String.valueOf(val))
        }
    }

    @CompileStatic
    private static toDouble(Object val) {
        if (val == null) {
            null
        } else if (val instanceof Number) {
            ((Number) val).doubleValue()
        } else {
            Double.parseDouble(String.valueOf(val))
        }
    }

    @CompileStatic
    String asString(int index, String column) {
        (String) getField(index, column)
    }

    /**
     * Returns the cell value of the specified row index and column name.
     *
     * @param index row index (0 - N-1 inclusively)
     * @param column column name.
     * @return cell value as object. Returns null if no column is found.
     */
    @CompileStatic
    Object getField(int index, String column) {
        getField(index, column, null)
    }

    /**
     * Returns the cell value of the specified row index and column name, if not found, will returns
     * default value.
     *
     * Note: Default value will not be returned if there is no such record for the given index.
     *
     * @param index row index (0 - N-1 inclusively)
     * @param column column name.
     * @param defValue default value.
     * @return cell value as object. Returns null if no column is found.
     */
    @CompileStatic
    Object getField(int index, String column, Object defValue) {
        get(index).getOrDefault(column, defValue)
    }

    /* **************************************************************************************************
     *
     * BELOW METHODS SHOULD NOT BE CALLED BY USER. THOSE ARE FOR EXECUTORS TO APPEND DATA.
     *
     *************************************************************************************************** */

    @PackageScope
    NyQLResult appendCount(int count) {
        add(Collections.singletonMap(JDBCConstants.COUNT_KEY, (Integer)count))
        this
    }

    @PackageScope
    NyQLResult appendCounts(int[] counts) {
        add(Collections.singletonMap(JDBCConstants.COUNT_KEY, Arrays.asList(counts)))
        this
    }

    @PackageScope
    NyQLResult appendCount(int count, List keys) {
        appendCount(count)
        if (QUtils.notNullNorEmpty(keys)) {
            add(Collections.singletonMap(JDBCConstants.KEYS_KEY, keys))
        }
        this
    }
}
