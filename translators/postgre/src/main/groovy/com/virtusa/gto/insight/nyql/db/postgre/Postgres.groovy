package com.virtusa.gto.insight.nyql.db.postgre

import com.virtusa.gto.insight.nyql.*
import com.virtusa.gto.insight.nyql.db.QDdl
import com.virtusa.gto.insight.nyql.db.QTranslator
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.blocks.AParam
import com.virtusa.gto.insight.nyql.utils.QUtils
import com.virtusa.gto.insight.nyql.utils.QueryType
import groovy.transform.PackageScope

import java.util.stream.Collectors

/**
 * @author Isuru Weerarathna
 */
class Postgres implements QTranslator, PostgresFunctions {

    private static final String DOUBLE_QUOTE = "\""
    private static final String STR_QUOTE = "'"

    @PackageScope Postgres() {}

    @Override
    def ___quoteString(final String text) {
        return QUtils.quote(text, STR_QUOTE)
    }

    @Override
    def ___convertBool(Boolean value) {
        return ___quoteString(value ? "t" : "f")
    }

    @Override
    def ___tableName(final Table table, final QContextType contextType) {
        if (contextType == QContextType.FROM) {
            return QUtils.quote(table.__name, DOUBLE_QUOTE) + (table.__aliasDefined() ? " " + table.__alias : "")
        } else {
            if (table.__aliasDefined()) {
                return table.__alias
            } else {
                return QUtils.quote(table.__name, DOUBLE_QUOTE)
            }
        }
    }

    @Override
    def ___tableJoinName(final Join join, final QContextType contextType, List<AParam> paramOrder) {
        // @TODO consider join type
        StringBuilder qstr = new StringBuilder();
        String jtype = invokeMethod(join.type, null)
        qstr.append(___deriveSource(join.table1, paramOrder, contextType))
                .append(" $jtype ")
                .append(___deriveSource(join.table2, paramOrder, contextType))

        if (join.___hasCondition()) {
            qstr.append(" ON ").append(___expandConditions(join.onConditions, paramOrder, QContextType.CONDITIONAL))
        }
        return qstr
    }

    @Override
    def ___columnName(final Column column, final QContextType contextType) {
        if (column instanceof Case) {
            return ___ifColumn(column)
        }

        if (contextType == QContextType.ORDER_BY) {
            if (column.__aliasDefined()) {
                return QUtils.quoteIfWS(column.__alias, DOUBLE_QUOTE)
            }
        }

        if (column instanceof FunctionColumn) {
            return this.invokeMethod(column._func, column._setOfCols ? column._columns : column._wrapper) + (column.__aliasDefined() ? " AS " + QUtils.quoteIfWS(column.__alias, DOUBLE_QUOTE) : "")
        } else {
            boolean tableHasAlias = column._owner != null && column._owner.__aliasDefined()
            if (tableHasAlias) {
                return column._owner.__alias + "." + column.__name
            } else {
                return ___tableName(column._owner, contextType) + "." + QUtils.quote(column.__name, DOUBLE_QUOTE)
            }
        }
    }

    @Override
    QResultProxy ___storedFunction(StoredFunction sp) {
        StringBuilder query = new StringBuilder()
        query.append("{ ? = CALL ").append(sp.name).append("( ")
        if (QUtils.notNullNorEmpty(sp.paramList)) {
            query.append(sp.paramList.stream().map({ "?" }).collect(Collectors.joining(", ")))
        }
        query.append(" ) }")

        return new QResultProxy(query: query.toString(), orderedParameters: sp.paramList, rawObject: sp, queryType: QueryType.DB_FUNCTION)
    }

    @Override
    QResultProxy ___updateQuery(QueryUpdate q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()
        query.append("UPDATE ").append(___deriveSource(q.sourceTbl, paramList, QContextType.FROM)).append("\n")
        if (q._assigns.__hasAssignments()) {
            query.append("SET ").append(___expandAssignments(q._assigns, paramList, QContextType.CONDITIONAL)).append("\n")
        }

        if (q._joiningTable != null) {
            // has joining tables
            List<Table> allTables = new ArrayList<>()
            List<String> allClauses = new ArrayList<>()
            QUtils.filterAllJoinConditions(q._joiningTable, allClauses, " AND ")
            QUtils.findAlTables(q._joiningTable, allTables)
            allTables.remove(q.sourceTbl)

            query.append("FROM ").append(allTables.stream().map({
                        t -> return ___tableName(t, QContextType.FROM)
                    }).collect(Collectors.joining(", "))).append("\n")

            if (q.whereObj == null) {
                q.whereObj = new Where(q._ctx)
            } else if (!q.whereObj.clauses.isEmpty() && !allClauses.isEmpty()) {
                q.whereObj.clauses.add(0, " AND ")
            }
            q.whereObj.clauses.addAll(0, allClauses)
        }


        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append("WHERE ").append(___expandConditions(q.whereObj, paramList, QContextType.CONDITIONAL)).append("\n")
        }

        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.UPDATE)
    }

    @Override
    QDdl ___ddls() {
        throw new NyException("Postgres does not support DDL statements!")
    }
}