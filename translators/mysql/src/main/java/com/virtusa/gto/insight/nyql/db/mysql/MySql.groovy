package com.virtusa.gto.insight.nyql.db.mysql

import com.virtusa.gto.insight.nyql.*
import com.virtusa.gto.insight.nyql.db.QDdl
import com.virtusa.gto.insight.nyql.db.QTranslator
import com.virtusa.gto.insight.nyql.utils.QUtils
import com.virtusa.gto.insight.nyql.utils.QueryType
import groovy.transform.PackageScope

import java.util.stream.Collectors

/**
 * @author Isuru Weerarathna
 */
class MySql implements QTranslator, MySqlFunctions {

    private static final MySqlDDL DDL = new MySqlDDL()

    static final String BACK_TICK = "`"
    static final String STR_QUOTE = "\""

    @PackageScope MySql() {}

    String NULL = "null"

    String JOIN(QContextType contextType) { "JOIN" }

    @Override
    def ___quoteString(final String text) {
        return QUtils.quote(text, STR_QUOTE)
    }

    @Override
    def ___convertBool(Boolean value) {
        return value != null && value ? "1" : "0"
    }

    @Override
    def ___tableName(final Table table, final QContextType contextType) {
        if (contextType == QContextType.FROM) {
            if (table.__isResultOf()) {
                QResultProxy proxy = table.__resultOf as QResultProxy
                return "(" + proxy.query.trim() + ")" + (table.__aliasDefined() ? " " + table.__alias : "")
            }
            return QUtils.quote(table.__name, BACK_TICK) + (table.__aliasDefined() ? " " + table.__alias : "")
        } else {
            if (table.__aliasDefined()) {
                return table.__alias
            } else {
                return QUtils.quote(table.__name, BACK_TICK)
            }
        }
    }

    @Override
    def ___tableJoinName(final Join join, final QContextType contextType, List<AParam> paramOrder=null) {
        StringBuilder qstr = new StringBuilder();
        String jtype = invokeMethod(join.type, null)

        if (join.table1.__isResultOf()) {
            QResultProxy proxy = join.table1.__resultOf as QResultProxy
            paramOrder?.addAll(proxy.orderedParameters)
        }
        qstr.append(___resolve(join.table1, contextType, paramOrder))
        qstr.append(" $jtype ")

        if (join.table2.__isResultOf()) {
            QResultProxy proxy = join.table2.__resultOf as QResultProxy
            paramOrder?.addAll(proxy.orderedParameters)
        }
        qstr.append(___resolve(join.table2, contextType, paramOrder))

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
                return QUtils.quoteIfWS(column.__alias, BACK_TICK)
            }
        }

        if (column instanceof FunctionColumn) {
            return this.invokeMethod(column._func, column._setOfCols ? column._columns : column._wrapper) + (column.__aliasDefined() ? " AS " + QUtils.quoteIfWS(column.__alias, BACK_TICK) : "")
        } else {
            boolean tableHasAlias = column._owner != null && column._owner.__aliasDefined()
            if (tableHasAlias) {
                return column._owner.__alias + "." + column.__name
            } else {
                if (column._owner != null) {
                    return ___tableName(column._owner, contextType) + "." + QUtils.quote(column.__name, BACK_TICK)
                } else {
                    return QUtils.quoteIfWS(column.__name, BACK_TICK)
                }
            }
        }
    }

    @Override
    QResultProxy ___updateQuery(QueryUpdate q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()

        if (q._joiningTable != null) {
            // has joining tables
            query.append("UPDATE ").append(___deriveSource(q._joiningTable, paramList, QContextType.FROM)).append("\n")
        } else {
            query.append("UPDATE ").append(___deriveSource(q.sourceTbl, paramList, QContextType.FROM)).append("\n")
        }

        if (q._assigns.__hasAssignments()) {
            query.append("SET ").append(___expandAssignments(q._assigns, paramList, QContextType.CONDITIONAL)).append("\n")
        }

        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append("WHERE ").append(___expandConditions(q.whereObj, paramList, QContextType.CONDITIONAL)).append("\n")
        }

        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.UPDATE)
    }

    @Override
    QResultProxy ___storedFunction(StoredFunction sp) {
        StringBuilder query = new StringBuilder()
        query.append("{ CALL ").append(sp.name).append("(")
        if (QUtils.notNullNorEmpty(sp.paramList)) {
            query.append(sp.paramList.stream().map({ "?" }).collect(Collectors.joining(", ")))
        }
        query.append(") }")

        return new QResultProxy(query: query.toString(), orderedParameters: sp.paramList,
                rawObject: sp, queryType: QueryType.DB_FUNCTION)
    }

    @Override
    QDdl ___ddls() {
        return DDL
    }
}