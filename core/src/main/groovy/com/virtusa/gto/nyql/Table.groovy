package com.virtusa.gto.nyql

import groovy.transform.CompileStatic

/**
 * @author Isuru Weerarathna
 */
@CompileStatic
class Table {

    private Map<String, Column> __allColumns = [:]

    QContext _ctx = null

    String __name = ''
    String __alias = null

    def __resultOf

    Table alias(String newName) {
        __alias = newName
        if (_ctx != null) {
            _ctx.tables.rename(__name, newName)
        }
        this
    }

    boolean __isResultOf() {
        __resultOf != null
    }

    boolean __aliasDefined() {
        __alias != null
    }

    Column COLUMN_AS(String name) {
        COLUMN_AS(name, null)
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
