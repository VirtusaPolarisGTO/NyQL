package com.virtusa.gto.insight.nyql.db

import com.virtusa.gto.insight.nyql.QResultProxy
import com.virtusa.gto.insight.nyql.ddl.DTable

/**
 * Interface to be implemented by any query generation which causes to change schema.
 *
 * @author IWEERARATHNA
 */
trait QDdl {

    /**
     * This method is called when a query is needed to generate a table creation
     * query.
     *
     * @param dTable table model instance to use for generation.
     * @return list of generated query statements.
     */
    abstract List<QResultProxy> ___createTable(DTable dTable)

    /**
     * This is called when it needs to generate a table drop query.
     *
     * @param dTable table model instance to use for generation.
     * @return list of generated query statements.
     */
    abstract List<QResultProxy> ___dropTable(DTable dTable)

    /**
     * Indicates how a model should be resolved for the given object.
     *
     * @param obj input object to resolve.
     * @return resolved object.
     */
    abstract def ___ddlResolve(Object obj)

}
