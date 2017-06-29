package com.virtusa.gto.nyql.db.maria

import com.virtusa.gto.nyql.CTE
import com.virtusa.gto.nyql.QResultProxy
import com.virtusa.gto.nyql.db.TranslatorOptions
import com.virtusa.gto.nyql.db.mysql.MySql
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.exceptions.NySyntaxException
import com.virtusa.gto.nyql.model.DbInfo

/**
 * @author iweerarathna
 */
class MariaSql extends MySql {

    MariaSql() {
        super()
    }

    MariaSql(TranslatorOptions theOptions, DbInfo dbInfo) {
        super(theOptions, dbInfo)
    }

    @Override
    List<QResultProxy> ___cteQuery(CTE cte) throws NyException {
        if (!isUnresolvedVersion(dbInfo) && dbInfo.majorVersion >= 10 && dbInfo.minorVersion >= 2) {
            return generateCTE(cte)
        } else {
            throw new NySyntaxException('MariaDB does not had support for Common Table Expressions prior to version 10.2!')
        }
    }
}
