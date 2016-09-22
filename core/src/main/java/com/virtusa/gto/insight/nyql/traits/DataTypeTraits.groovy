package com.virtusa.gto.insight.nyql.traits

import com.virtusa.gto.insight.nyql.FunctionColumn

/**
 * @author IWEERARATHNA
 */
trait DataTypeTraits {

    def STR(String text) {
        return _ctx.translator.___quoteString(text)
    }

    def BETWEEN(Object c1, Object c2) {
        return new FunctionColumn(_columns: [c1,c2], _setOfCols: true, _func: "between", _ctx: _ctx)
    }

    def LIKE(Object comp) {
        return new FunctionColumn(_ctx: _ctx, _setOfCols: true, _columns: [comp], _func: "like")
    }

    def NOTLIKE(Object comp) {
        return new FunctionColumn(_ctx: _ctx, _setOfCols: true, _columns: [comp], _func: "not_like")
    }

}