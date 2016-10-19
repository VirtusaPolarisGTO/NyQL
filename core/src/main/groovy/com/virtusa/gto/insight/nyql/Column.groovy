package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.exceptions.NySyntaxException
import com.virtusa.gto.insight.nyql.utils.QUtils

/**
 * @author Isuru Weerarathna
 */
class Column {

    QContext _ctx = null
    Table _owner = null

    String __name = ''
    String __alias = null

    def alias(String newName) {
        _ctx.renameColumn(__alias, newName, this)
        __alias = newName
        return this
    }

    def __aliasDefined() {
        return __alias != null
    }

    Column plus(Object other) {
        return new FunctionColumn(_columns: [this, other], _func: 'op_add', _setOfCols: true, _ctx: _ctx)
    }

    Column minus(Object other) {
        return new FunctionColumn(_columns: [this, other], _func: 'op_minus', _setOfCols: true, _ctx: _ctx)
    }

    Column multiply(Object other) {
        return new FunctionColumn(_columns: [this, other], _func: 'op_multiply', _setOfCols: true, _ctx: _ctx)
    }

    Column div(Object other) {
        return new FunctionColumn(_columns: [this, other], _func: 'op_divide', _setOfCols: true, _ctx: _ctx)
    }

    Column mod(Object other) {
        return new FunctionColumn(_columns: [this, other], _func: 'op_modulus', _setOfCols: true, _ctx: _ctx)
    }

    Column and(Object other) {
        new FunctionColumn(_columns: [this, other], _func: 'op_bit_and', _setOfCols: true, _ctx: _ctx)
    }

    Column or(Object other) {
        new FunctionColumn(_columns: [this, other], _func: 'op_bit_or', _setOfCols: true, _ctx: _ctx)
    }

    Column xor(Object other) {
        new FunctionColumn(_columns: [this, other], _func: 'op_bit_xor', _setOfCols: true, _ctx: _ctx)
    }

    def propertyMissing(String name) {
        throw new NySyntaxException(QUtils.generateErrStr(
                "You cannot refer to a column called '$name' inside the column '${this.__name}'!",
                "Did you spell the table name correctly? [Table Name: ${this.__name}]",
                "Or, is table missing from your query context? If so use EXPECT to declare the table name!"
        ))
    }
}
