package com.virtusa.gto.insight.nyql
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
        return new Join(table1: this, table2: t, _ctx: _ctx)
    }

    /**
     * Inner join
     *
     * @param t other table to be joined with.
     * @return new join instance
     */
    def INNER_JOIN(Table t) {
        return new Join(table1: this, table2: t, _ctx: _ctx, type: "INNER_JOIN")
    }

    def LEFT_OUTER_JOIN(Table t) {
        return new Join(table1: this, table2: t, _ctx: _ctx, type: "LEFT_OUTER_JOIN")
    }

    def RIGHT_OUTER_JOIN(Table t) {
        return new Join(table1: this, table2: t, _ctx: _ctx, type: "RIGHT_OUTER_JOIN")
    }

    def RIGHT_JOIN(Table t) {
        return new Join(table1: this, table2: t, _ctx: _ctx, type: "RIGHT_JOIN")
    }

    def LEFT_JOIN(Table t) {
        return new Join(table1: this, table2: t, _ctx: _ctx, type: "LEFT_JOIN")
    }

    def FULL_OUTER_JOIN(Table t) {
        return new Join(table1: this, table2: t, _ctx: _ctx, type: "FULL_OUTER_JOIN")
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

    def propertyMissing(String name) {
        Column oCol = __allColumns.get(name)
        if (oCol != null) {
            return oCol
        }
        Column col = new Column(__name: name, _owner: this, _ctx: _ctx)
        __allColumns.put(name, col)
        return col
    }

}
