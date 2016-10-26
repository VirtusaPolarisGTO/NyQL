package com.virtusa.gto.nyql.traits

import com.virtusa.gto.nyql.FunctionColumn
import com.virtusa.gto.nyql.model.units.QNumber
import com.virtusa.gto.nyql.model.units.QString
import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
trait DataTypeTraits {

    @CompileStatic
    QString STR(String text) {
        new QString(text: text)
    }

    @CompileStatic
    QNumber NUM(Number number) {
        new QNumber(number: number)
    }

    def BETWEEN(Object c1, Object c2) {
        return new FunctionColumn(_columns: [c1, c2], _setOfCols: true, _func: 'between', _ctx: _ctx)
    }

    def NOT_BETWEEN(Object c1, Object c2) {
        return new FunctionColumn(_columns: [c1,c2], _setOfCols: true, _func: 'not_between', _ctx: _ctx)
    }

    def LIKE(Object comp) {
        return new FunctionColumn(_ctx: _ctx, _setOfCols: true, _columns: [comp], _func: 'like')
    }

    def NOTLIKE(Object comp) {
        return new FunctionColumn(_ctx: _ctx, _setOfCols: true, _columns: [comp], _func: 'not_like')
    }

}