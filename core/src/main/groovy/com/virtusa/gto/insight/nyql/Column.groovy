package com.virtusa.gto.insight.nyql
/**
 * @author Isuru Weerarathna
 */
class Column {

    QContext _ctx = null
    Table _owner = null

    String __name = ""
    String __alias = null

    def alias(String newName) {
        _ctx?.renameColumn(__alias, newName, this)
        __alias = newName
        return this
    }

    def __aliasDefined() {
        return __alias != null
    }

    Column plus(Object other) {
        return new FunctionColumn(_columns: [this, other], _func: "op_add", _setOfCols: true, _ctx: _ctx)
    }

    Column minus(Object other) {
        return new FunctionColumn(_columns: [this, other], _func: "op_minus", _setOfCols: true, _ctx: _ctx)
    }

    Column multiply(Object other) {
        return new FunctionColumn(_columns: [this, other], _func: "op_multiply", _setOfCols: true, _ctx: _ctx)
    }

    Column div(Object other) {
        return new FunctionColumn(_columns: [this, other], _func: "op_divide", _setOfCols: true, _ctx: _ctx)
    }

    Column mod(Object other) {
        return new FunctionColumn(_columns: [this, other], _func: "op_modulus", _setOfCols: true, _ctx: _ctx)
    }
}
