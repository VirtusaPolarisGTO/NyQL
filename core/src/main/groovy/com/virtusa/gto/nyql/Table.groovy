package com.virtusa.gto.nyql

import groovy.transform.CompileStatic

/**
 * @author Isuru Weerarathna
 */
@CompileStatic
class Table {

    protected Map<String, Column> __allColumns = [:]

    QContext _ctx = null

    String __schema = null
    String __name = ''
    String __alias = null

    def __resultOf

    TableAll ALL() {
        TableAll tableAll = new TableAll()
        tableAll.__alias = this.__alias
        tableAll.__name = this.__name
        tableAll.__schema = this.__schema
        tableAll.__resultOf = this.__resultOf
        tableAll._ctx = this._ctx
        tableAll.__allColumns = this.__allColumns
        tableAll
    }

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
