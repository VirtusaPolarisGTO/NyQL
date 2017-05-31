package com.virtusa.gto.nyql.db.mysql

import com.virtusa.gto.nyql.Join
import com.virtusa.gto.nyql.QuerySelect
import com.virtusa.gto.nyql.Table
import com.virtusa.gto.nyql.model.JoinType
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

/**
 * @author iweerarathna
 */
@CompileStatic
@PackageScope
class MySqlUtils {

    static int countFullJoin(Table table) {
        if (table == null) {
            return 0
        }

        if (table instanceof Join) {
            Join join = (Join)table
            if (join.type == JoinType.FULL_JOIN) {
                return 1 + countFullJoin(join.table1)
            } else {
                return countFullJoin(join.table1)
            }
        } else {
            return 0
        }
    }

    static void findFullJoins(Table table, List<Table> fulljoins) {
        if (table == null) {
            return
        }

        if (table instanceof Join) {
            Join join = (Join)table
            if (join.type == JoinType.FULL_JOIN) {
                fulljoins.add(join)
            }
            findFullJoins(join.table1, fulljoins)
        }
    }

    static QuerySelect cloneQuery(QuerySelect input) {
        QuerySelect q = new QuerySelect(input._ctx)
        q._distinct = input._distinct
        q._intoColumns = input._intoColumns
        q._intoTable = input._intoTable
        q._joiningTable = cloneJoin(input._joiningTable)
        q.groupBy = input.groupBy
        q.groupByRollup = input.groupByRollup
        q.groupHaving = input.groupHaving
        q.offset = input.offset
        q.orderBy = input.orderBy
        q.projection = input.projection
        q._limit = input._limit
        q.sourceTbl = input.sourceTbl
        q.returnType = input.returnType
        q.whereObj = input.whereObj
        return q
    }

    static Table cloneJoin(Table org) {
        if (org == null) {
            return null
        }
        if (org instanceof Join) {
            Join join = (Join)org
            Join cloned = join.type == JoinType.FULL_JOIN ? new MySqlFullJoin() : new Join()
            cloned.table1 = cloneJoin(join.table1)
            cloned.table2 = join.table2
            cloned.type = join.type == JoinType.FULL_JOIN ? JoinType.RIGHT_JOIN : join.type
            cloned._ctx = join._ctx
            cloned.onConditions = join.onConditions
            cloned.__alias = join.__alias
            cloned.__name = join.__name
            cloned.__resultOf = join.__resultOf

            return cloned
        } else {
            return org
        }
    }

    static Table flipNthFullJoin(Table org, int n, int curr) {
        if (n == 0 || org == null) {
            return org
        }

        if (org instanceof Join) {
            if (org instanceof MySqlFullJoin) {
                curr++
                if (n >= curr) {
                    ((MySqlFullJoin)org).type = JoinType.LEFT_JOIN
                }
            }
            flipNthFullJoin(((Join)org).table1, n, curr)
        }
        return org
    }

}
