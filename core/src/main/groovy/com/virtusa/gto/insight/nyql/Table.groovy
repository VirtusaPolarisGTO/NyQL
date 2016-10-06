package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.exceptions.NySyntaxException
import com.virtusa.gto.insight.nyql.utils.QUtils

/**
 * @author Isuru Weerarathna
 */

class Table {

    /**
     * Represents any table replaceable with any table in joins.
     */
    static final Table ANY_TABLE = new Table()

    private Map<String, Column> __allColumns = new HashMap<>()

    QContext _ctx = null

    def __name = ""
    def __alias = null

    def __resultOf

    /**
     * This is cross join.
     *
     * @param t other table to be joined with
     * @return new join instance
     */
    def JOIN(Table t) {
        return INNER_JOIN(t)
    }

    /**
     * Inner join
     *
     * @param t other table to be joined with.
     * @return new join instance
     */
    def INNER_JOIN(Table t) {
        return QUtils.mergeJoinClauses(_ctx, this, t, "INNER_JOIN")
    }

    def LEFT_OUTER_JOIN(Table t) {
        return QUtils.mergeJoinClauses(_ctx, this, t, "LEFT_OUTER_JOIN")
    }

    def RIGHT_OUTER_JOIN(Table t) {
        return QUtils.mergeJoinClauses(_ctx, this, t, "RIGHT_OUTER_JOIN")
    }

    def RIGHT_JOIN(Table t) {
        return QUtils.mergeJoinClauses(_ctx, this, t, "RIGHT_JOIN")
    }

    def LEFT_JOIN(Table t) {
        return QUtils.mergeJoinClauses(_ctx, this, t, "LEFT_JOIN")
    }

    def alias(String newName) {
        __alias = newName
        if (_ctx != null) {
            _ctx.tables.rename(__name, newName)
        }
        return this
    }

    boolean __isResultOf() {
        return __resultOf != null
    }

    def __aliasDefined() {
        return __alias != null
    }

    Column COLUMN(String name) {
        Column oCol = __allColumns.get(name)
        if (oCol != null) {
            return oCol
        }
        Column col = new Column(__name: name, _owner: this, _ctx: _ctx)
        __allColumns.put(name, col)
        return col
    }

    def propertyMissing(String name) {
        return COLUMN(name)
    }

}
