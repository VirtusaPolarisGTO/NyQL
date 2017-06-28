package com.virtusa.gto.nyql.db.h2

import com.virtusa.gto.nyql.QResultProxy
import com.virtusa.gto.nyql.db.QDdl
import com.virtusa.gto.nyql.ddl.DTable

/**
 * @author iweerarathna
 */
class H2DDL implements QDdl {

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
