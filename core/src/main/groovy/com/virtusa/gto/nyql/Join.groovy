package com.virtusa.gto.nyql

import com.virtusa.gto.nyql.model.JoinType
import com.virtusa.gto.nyql.utils.QOperator
import groovy.transform.CompileStatic

/**
 * @author Isuru Weerarathna
 */
@CompileStatic
class Join extends Table {

    JoinType type = JoinType.CROSS_JOIN

    Table table1
    Table table2
    Where onConditions = null

    Join ON(String expr) {
        ___initClauses()
        onConditions.RAW(expr)
        this
    }

    Join ON(Column c1, QOperator op = QOperator.EQUAL, Column c2) {
        ___initClauses()
        onConditions.ON(c1, op, c2)
        this
    }

    Join ON(@DelegatesTo(value = Where, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        Where whr = new Where(_ctx)

        def code = closure.rehydrate(whr, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        onConditions = whr
        this
    }

    boolean ___hasCondition() {
        onConditions != null
    }

    private void ___initClauses() {
        if (onConditions == null) {
            onConditions = new Where(_ctx)
        }
    }

}
