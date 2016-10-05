package com.virtusa.gto.insight.nyql.db.mssql

import com.virtusa.gto.insight.nyql.QResultProxy
import com.virtusa.gto.insight.nyql.db.QDdl
import com.virtusa.gto.insight.nyql.ddl.DTable

/**
 * @author IWEERARATHNA
 */
class MSSqlDDL implements QDdl {
    @Override
    List<QResultProxy> ___createTable(DTable dTable) {
        return null
    }

    @Override
    List<QResultProxy> ___dropTable(DTable dTable) {
        return null
    }

    @Override
    def ___ddlResolve(Object obj) {
        return null
    }
}
