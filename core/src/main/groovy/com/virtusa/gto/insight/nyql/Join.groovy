package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.utils.QOperator

/**
 * @author Isuru Weerarathna
 */
class Join extends Table {

    String type = "JOIN"

    Table table1
    Table table2
    Where onConditions = null

    def ON(String expr) {
        ___initClauses()
        onConditions.RAW(expr)
        return this
    }

    def ON(Column c1, QOperator op = QOperator.EQUAL, Column c2) {
        ___initClauses()
        onConditions.ON(c1, op, c2)
        return this
    }

    def ON(closure) {
        Where whr = new Where(_ctx)

        def code = closure.rehydrate(whr, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        onConditions = whr
        return this
    }

    def ___hasCondition() {
        return onConditions != null
    }

    private def ___initClauses() {
        if (onConditions == null) {
            onConditions = new Where(_ctx)
        }
    }

}
