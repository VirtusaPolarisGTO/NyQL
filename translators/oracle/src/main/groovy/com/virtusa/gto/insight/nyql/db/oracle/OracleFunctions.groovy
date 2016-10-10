package com.virtusa.gto.insight.nyql.db.oracle

import com.virtusa.gto.insight.nyql.db.QFunctions

/**
 * @author IWEERARATHNA
 */
class OracleFunctions implements QFunctions {

    @Override
    def date_trunc(Object it) {
        return null
    }

    @Override
    def substr(Object c) {
        if (c instanceof List) {
            return 'SUBSTR(' + ___resolveIn(c[0]) + ', ' + ___resolveIn(c[1]) +
                    (c.size() > 2 ? ', ' + ___resolveIn(c[2]) : '') + ')'
        }
    }

    @Override
    def position(Object c) {
        if (c instanceof List) {
            return 'instr(' + ___resolveIn(c[0]) + ', ' + ___resolveIn(c[1]) + ')'
        }
    }
}
