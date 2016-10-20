package com.virtusa.gto.insight.nyql.db.oracle

import com.virtusa.gto.insight.nyql.db.QFunctions

/**
 * @author IWEERARATHNA
 */
class OracleFunctions implements QFunctions {

    @Override
    String date_trunc(Object it) {
        return null
    }

    @Override
    String substr(Object c) {
        if (c instanceof List) {
            return 'SUBSTR(' + ___resolveIn(c[0]) + ', ' + ___resolveIn(c[1]) +
                    (c.size() > 2 ? ', ' + ___resolveIn(c[2]) : '') + ')'
        }
    }

    @Override
    String position(Object c) {
        if (c instanceof List) {
            return 'instr(' + ___resolveIn(c[0]) + ', ' + ___resolveIn(c[1]) + ')'
        }
    }

    @Override
    String cast_to_int(Object col) {
        return null
    }

    @Override
    String cast_to_str(Object col) {
        return null
    }
}
