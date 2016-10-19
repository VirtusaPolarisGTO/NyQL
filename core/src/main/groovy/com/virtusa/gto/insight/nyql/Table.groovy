package com.virtusa.gto.insight.nyql
/**
 * @author Isuru Weerarathna
 */

class Table {

    private Map<String, Column> __allColumns = [:]

    QContext _ctx = null

    def __name = ''
    def __alias = null

    def __resultOf

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

    Column COLUMN_AS(String name, String alias) {
        Column column = new Column(__name: name, __alias: alias, _owner: this, _ctx: _ctx)
        __allColumns.put(name + '::' + alias, column)
        column
    }

    Column COLUMN(String name) {
        Column oCol = __allColumns.get(name)
        if (oCol != null) {
            return oCol
        }
        Column col = new Column(__name: name, _owner: this, _ctx: _ctx)
        __allColumns.put(name, col)
        col
    }

    def propertyMissing(String name) {
        COLUMN(name)
    }

}
