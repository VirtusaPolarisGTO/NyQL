package com.virtusa.gto.nyql.db.oracle

import com.virtusa.gto.nyql.db.QFunctions
import com.virtusa.gto.nyql.exceptions.NyException

/**
 * @author IWEERARATHNA
 */
class OracleFunctions implements QFunctions {

    @Override
    String str_replace(Object c) {
        if (c instanceof List) {
            return 'REPLACE(' + ___resolveIn(c[0]) + ', ' + ___resolveIn(c[1]) + ', ' + ___resolveIn(c[2]) + ')'
        }
        throw new NyException('Incorrect number of parameters for string replace function!')
    }

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

    @Override
    String cast_to_date(Object col) {
        return null
    }
}
