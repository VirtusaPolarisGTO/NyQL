package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.exceptions.NySyntaxException
import com.virtusa.gto.insight.nyql.utils.QUtils

/**
 * @author Isuru Weerarathna
 */

class Table {

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
        return mergeJoinClauses(this, t, "INNER_JOIN")
    }

    def LEFT_OUTER_JOIN(Table t) {
        return mergeJoinClauses(this, t, "LEFT_OUTER_JOIN")
    }

    def RIGHT_OUTER_JOIN(Table t) {
        return mergeJoinClauses(this, t, "RIGHT_OUTER_JOIN")
    }

    def RIGHT_JOIN(Table t) {
        return mergeJoinClauses(this, t, "RIGHT_JOIN")
    }

    def LEFT_JOIN(Table t) {
        return mergeJoinClauses(this, t, "LEFT_JOIN")
    }

    def FULL_OUTER_JOIN(Table t) {
        return mergeJoinClauses(this, t, "FULL_OUTER_JOIN")
    }

    def FULL_JOIN(Table t) {
        return mergeJoinClauses(this, t, "FULL_JOIN")
    }

    Table mergeJoinClauses(Table table1, Table table2, String type) {
        Table rmost = QUtils.findRightMostTable(table1)
        Table lmost = QUtils.findLeftMostTable(table2)
        if (rmost.__name == lmost.__name && rmost.__alias == lmost.__alias) {
            if (table1 instanceof Join) {
                if (table2 instanceof Join) {
                    return new Join(table1: table1, table2: table2.table2, _ctx: _ctx, type: type)
                } else {
                    return table1
                }
            } else {
                if (table2 instanceof Join) {
                    return table2
                } else {
                    throw new NySyntaxException("Merging same table!")
                }
            }
        }
        return new Join(table1: table1, table2: table2, _ctx: _ctx, type: type)
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
