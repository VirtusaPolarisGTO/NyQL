package com.virtusa.gto.insight.nyql.db

import com.virtusa.gto.insight.nyql.QResultProxy
import com.virtusa.gto.insight.nyql.ddl.DTable

/**
 * @author IWEERARATHNA
 */
trait QDdl {

    abstract QResultProxy ___createTable(DTable dTable)

    abstract QResultProxy ___dropTable(DTable dTable)

    abstract def ___ddlResolve(Object obj)

}
