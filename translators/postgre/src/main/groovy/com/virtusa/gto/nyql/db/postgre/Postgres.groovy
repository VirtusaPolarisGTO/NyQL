package com.virtusa.gto.nyql.db.postgre

import com.virtusa.gto.nyql.Case
import com.virtusa.gto.nyql.Column
import com.virtusa.gto.nyql.FunctionColumn
import com.virtusa.gto.nyql.Join
import com.virtusa.gto.nyql.QContextType
import com.virtusa.gto.nyql.QResultProxy
import com.virtusa.gto.nyql.QueryDelete
import com.virtusa.gto.nyql.QueryInsert
import com.virtusa.gto.nyql.QueryPart
import com.virtusa.gto.nyql.QuerySelect
import com.virtusa.gto.nyql.QueryTruncate
import com.virtusa.gto.nyql.QueryUpdate
import com.virtusa.gto.nyql.StoredFunction
import com.virtusa.gto.nyql.Table
import com.virtusa.gto.nyql.Where
import com.virtusa.gto.nyql.db.QDdl
import com.virtusa.gto.nyql.db.QTranslator
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.units.AParam
import com.virtusa.gto.nyql.utils.QUtils
import com.virtusa.gto.nyql.utils.QueryCombineType
import com.virtusa.gto.nyql.utils.QueryType
import groovy.transform.CompileStatic

import java.util.stream.Collectors
import java.util.stream.Stream
/**
 * @author Isuru Weerarathna
 */
class Postgres extends PostgresFunctions implements QTranslator {

    private static final PostgreDDL DDL = new PostgreDDL()

    static final String DOUBLE_QUOTE = "\""
    static final String STR_QUOTE = "'"

    private static final String NL = '\n'
    private static final String COMMA = ', '
    static final String OP = '('
    static final String CP = ')'
    private static final String _AS_ = ' AS '

    private static String TRUE_STR = 't'
    private static String FALSE_STR = 'f'

    Postgres() {}

    @CompileStatic
    @Override
    String ___ifColumn(Case aCaseCol, List<AParam> paramOrder) {
        StringBuilder query = new StringBuilder('CASE')
        List<Case.CaseCondition> conditions = aCaseCol.allConditions
        for (Case.CaseCondition cc : conditions) {
            query.append(' WHEN ').append(___expandConditions(cc._theCondition, paramOrder, QContextType.CONDITIONAL))
            query.append(' THEN ').append(___resolve(cc._theResult, QContextType.SELECT))
        }

        if (aCaseCol.getElse() != null) {
            query.append(' ELSE ').append(___resolve(aCaseCol.getElse(), QContextType.SELECT))
        }
        query.append(' END')

        if (aCaseCol.__aliasDefined()) {
            query.append(_AS_).append(QUtils.quoteIfWS(aCaseCol.__alias, DOUBLE_QUOTE))
        }
        query.toString()
    }

    @CompileStatic
    @Override
    String ___quoteString(final String text) {
        return QUtils.quote(text, STR_QUOTE)
    }

    @CompileStatic
    @Override
    String ___convertBool(Boolean value) {
        return ___quoteString(value ? TRUE_STR : FALSE_STR)
    }

    @CompileStatic
    @Override
    String ___tableName(final Table table, final QContextType contextType) {
        if (contextType == QContextType.INTO || contextType == QContextType.TRUNCATE
                || contextType == QContextType.DELETE_FROM) {
            return QUtils.quote(table.__name)
        } else if (contextType == QContextType.FROM || contextType == QContextType.UPDATE_FROM
                || contextType == QContextType.DELETE_JOIN) {
            if (table.__isResultOf()) {
                QResultProxy proxy = table.__resultOf as QResultProxy
                return QUtils.parenthesis(proxy.query.trim()) + (table.__aliasDefined() ? ' ' + table.__alias : '')
            }
            return QUtils.quote(table.__name, DOUBLE_QUOTE) + (table.__aliasDefined() ? ' ' + table.__alias : '')
        } else if (contextType == QContextType.SELECT || contextType == QContextType.INSERT_DATA || contextType == QContextType.UPDATE_SET) {
            if (table.__isResultOf()) {
                QResultProxy proxy = table.__resultOf as QResultProxy
                return QUtils.parenthesis(proxy.query.trim()) + (table.__aliasDefined() ? _AS_ + table.__alias : '')
            }
        }

        if (table.__aliasDefined()) {
            return table.__alias
        } else {
            return QUtils.quote(table.__name, DOUBLE_QUOTE)
        }
    }

    @CompileStatic
    @Override
    String ___tableJoinName(final Join join, final QContextType contextType, List<AParam> paramOrder) {
        StringBuilder qstr = new StringBuilder()
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
            qstr.append(' ON ').append(___expandConditions(join.onConditions, paramOrder, QUtils.findDeleteContext(contextType)))
        }
        qstr
    }

    @CompileStatic
    @Override
    String ___columnName(final Column column, final QContextType contextType, List<AParam> paramList) {
        if (contextType == QContextType.ORDER_BY || contextType == QContextType.GROUP_BY || contextType == QContextType.HAVING) {
            if (column.__aliasDefined()) {
                return QUtils.quoteIfWS(column.__alias, DOUBLE_QUOTE)
            }
        }

        if (column instanceof Case) {
            return ___ifColumn(column, null)
        }

        if (contextType == QContextType.INTO || contextType == QContextType.INSERT_PROJECTION) {
            return QUtils.quote(column.__name, DOUBLE_QUOTE)
        }

        if (contextType == QContextType.DELETE_CONDITIONAL_JOIN) {
            if (column._owner.__aliasDefined()) {
                return QUtils.quoteIfWS(column._owner.__alias, DOUBLE_QUOTE) + "." + QUtils.quoteIfWS(column.__name, DOUBLE_QUOTE)
            }
            return QUtils.quote(column._owner.__name, DOUBLE_QUOTE) + "." + QUtils.quoteIfWS(column.__name, DOUBLE_QUOTE)
        } else if (contextType == QContextType.DELETE_CONDITIONAL) {
            return QUtils.quote(column._owner.__name, DOUBLE_QUOTE) + "." + QUtils.quoteIfWS(column.__name, DOUBLE_QUOTE)
        }

        if (column instanceof FunctionColumn) {
            return String.valueOf(this.invokeMethod(column._func, column._setOfCols ? column._columns : column._wrapper)) +
                    (column.__aliasDefined() ? _AS_ + QUtils.quoteIfWS(column.__alias, DOUBLE_QUOTE) : '')
        } else {
            boolean tableHasAlias = column._owner != null && column._owner.__aliasDefined()
            if (tableHasAlias) {
                return column._owner.__alias + "." + column.__name +
                        (column.__aliasDefined() && contextType == QContextType.SELECT ?
                                _AS_ + QUtils.quoteIfWS(column.__alias, DOUBLE_QUOTE) : '')
            } else {
                return QUtils.quoteIfWS(column.__name, DOUBLE_QUOTE) +
                        (column.__aliasDefined() && contextType == QContextType.SELECT ?
                                _AS_ + QUtils.quoteIfWS(column.__alias, DOUBLE_QUOTE) : '')
            }
        }
    }

    @CompileStatic
    @Override
    QResultProxy ___partQuery(QueryPart q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()
        QueryType queryType = QueryType.PART

        if (q._allProjections != null) {
            query.append(___expandProjection(q._allProjections, paramList))
            return new QResultProxy(query: query.toString(), queryType: queryType,
                    orderedParameters: paramList, rawObject: q._allProjections, qObject: q)
        }

        if (q.sourceTbl != null) {
            query.append(___deriveSource(q.sourceTbl, paramList, QContextType.FROM))
            return new QResultProxy(query: query.toString(), queryType: queryType,
                    orderedParameters: paramList, rawObject: q.sourceTbl, qObject: q)
        }

        if (q.whereObj != null) {
            query.append(___expandConditions(q.whereObj, paramList, QContextType.CONDITIONAL))
            return new QResultProxy(query: query.toString(), queryType: queryType,
                    orderedParameters: paramList, rawObject: q.whereObj, qObject: q)
        }

        if (q._assigns != null) {
            query.append(___expandAssignments(q._assigns, paramList, QContextType.UPDATE_SET))
            return new QResultProxy(query: query.toString(), queryType: queryType,
                    orderedParameters: paramList, rawObject: q._assigns, qObject: q)
        }

        if (QUtils.notNullNorEmpty(q._intoColumns)) {
            //query.append(___expandProjection(q._intoColumns, paramList, QContextType.INSERT_PROJECTION))
            return new QResultProxy(query: query.toString(), queryType: queryType,
                    orderedParameters: paramList, rawObject: q._intoColumns, qObject: q)
        }

        if (!QUtils.isNullOrEmpty(q._dataColumns)) {
            return new QResultProxy(query: '', queryType: queryType,
                    orderedParameters: paramList, rawObject: q._dataColumns, qObject: q)
        }
        throw new NyException('Parts are no longer supports to reuse other than WHERE and JOINING!')
    }

    @Override
    QResultProxy ___deleteQuery(QueryDelete q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()
        query.append('DELETE FROM ').append(___deriveSource(q.sourceTbl, paramList, QContextType.FROM)).append('\n')
        if (q._joiningTable != null) {
            // has joining tables
            List<Table> allTables = new ArrayList<>()
            List<Object> allClauses = new ArrayList<>()
            QUtils.filterAllJoinConditions(q._joiningTable, allClauses, ' AND ')
            QUtils.findAlTables(q._joiningTable, allTables)
            allTables.remove(q.sourceTbl)

            query.append('USING ').append(allTables.stream().map({
                t -> return ___tableName(t, QContextType.FROM)
            }).collect(Collectors.joining(', '))).append('\n')

            if (q.whereObj == null) {
                q.whereObj = new Where(q._ctx)
            } else if (!q.whereObj.clauses.isEmpty() && !allClauses.isEmpty()) {
                q.whereObj.clauses.add(0, ' AND ')
            }
            q.whereObj.clauses.addAll(0, allClauses)
        }

        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append(' WHERE ').append(___expandConditions(q.whereObj, paramList, QContextType.CONDITIONAL)).append('\n')
        }
        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.DELETE);
    }

    @Override
    QResultProxy ___truncateQuery(QueryTruncate q) {
        StringBuilder query = new StringBuilder()
        query.append('TRUNCATE TABLE ').append(___tableName(q.sourceTbl, QContextType.TRUNCATE))

        new QResultProxy(query: query.toString(), orderedParameters: [], queryType: QueryType.TRUNCATE)
    }

    @Override
    QResultProxy ___combinationQuery(QueryCombineType combineType, List<Object> queries) {
        List<AParam> paramList = new LinkedList<>()
        Stream<Object> stream = queries.stream().map({ q ->
            if (q instanceof QResultProxy) {
                paramList.addAll(q.orderedParameters ?: [])
                return "(" + ___resolve(q, QContextType.UNKNOWN) + ")"
            } else {
                return ___resolve(q, QContextType.UNKNOWN, paramList)
            }
        });

        String qStr;
        if (combineType == QueryCombineType.UNION) {
            qStr = stream.collect(Collectors.joining("\nUNION ALL\n"))
        } else if (combineType == QueryCombineType.UNION_DISTINCT) {
            qStr = stream.collect(Collectors.joining("\nUNION\n"))
        } else if (combineType == QueryCombineType.INTERSECT) {
            qStr = stream.collect(Collectors.joining(NL + 'INTERSECT' + NL))
        } else {
            qStr = stream.collect(Collectors.joining("; "))
        }
        return new QResultProxy(query: qStr, orderedParameters: paramList, queryType: QueryType.SELECT, qObject: queries)
    }

    @Override
    QResultProxy ___selectQuery(QuerySelect q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()
        QueryType queryType = QueryType.SELECT
        if (q._intoTable != null) {
            queryType = QueryType.INSERT
            query.append("INSERT INTO ").append(___tableName(q._intoTable, QContextType.INTO)).append(" ")
            if (QUtils.notNullNorEmpty(q._intoColumns)) {
                query.append(q._intoColumns.stream().map({
                    return ___columnName(it, QContextType.INTO)
                }).collect(Collectors.joining(", ", "(", ")")))
            }
            query.append("\n")
        }

        query.append("SELECT ");
        if (q._distinct) {
            query.append("DISTINCT ")
        }
        query.append(___expandProjection(q.projection, paramList)).append("\n")
        query.append(" FROM ").append(___deriveSource(q._joiningTable ?: q.sourceTbl, paramList, QContextType.FROM)).append("\n")

        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append(" WHERE ").append(___expandConditions(q.whereObj, paramList, QContextType.CONDITIONAL)).append("\n")
        }

        if (QUtils.notNullNorEmpty(q.groupBy)) {
            query.append(" GROUP BY ").append(q.groupBy.stream()
                    .map({ Object t -> return ___columnName(t, QContextType.GROUP_BY) })
                    .collect(Collectors.joining(", ")))

            if (q.groupHaving != null) {
                query.append("\n").append(" HAVING ").append(___expandConditions(q.groupHaving, paramList, QContextType.GROUP_BY))
            }
            query.append("\n")
        }

        if (QUtils.notNullNorEmpty(q.orderBy)) {
            query.append(" ORDER BY ").append(q.orderBy.stream()
                    .map({ t -> return ___columnName(t, QContextType.ORDER_BY) })
                    .collect(Collectors.joining(", ")))
                    .append("\n")
        }

        if (q._limit != null) {
            if (q._limit instanceof Integer && ((Integer) q._limit) > 0) {
                query.append(" LIMIT ").append(String.valueOf(q._limit)).append("\n")
            } else if (q._limit instanceof AParam) {
                paramList.add((AParam) q._limit)
                query.append(" LIMIT ").append(___resolve(q._limit, QContextType.ORDER_BY)).append("\n")
            }
        }

        if (q.offset != null) {
            if (q.offset instanceof Integer && ((Integer) q.offset) >= 0) {
                query.append(" OFFSET ").append(String.valueOf(q.offset)).append("\n")
            } else if (q.offset instanceof AParam) {
                paramList.add((AParam) q.offset)
                query.append(" OFFSET ").append(___resolve(q.offset, QContextType.ORDER_BY)).append("\n")
            }
        }

        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: queryType, qObject: q)
    }

    @Override
    QResultProxy ___insertQuery(QueryInsert q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()

        query.append("INSERT INTO ").append(___resolve(q._targetTable, QContextType.INTO, paramList)).append(" (")
        List<String> colList = new LinkedList<>()
        List<String> valList = new LinkedList<>()
        q._data.each { k, v ->
            colList.add(k)

            if (v instanceof AParam) {
                paramList.add((AParam)v)
            }
            valList.add(String.valueOf(___resolve(v, QContextType.CONDITIONAL)))
        }
        query.append(colList.stream().collect(Collectors.joining(", ")))
                .append(") VALUES (")
                .append(valList.stream().collect(Collectors.joining(", ")))
                .append(")")

        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.INSERT, qObject: q)
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
        return DDL
    }

    private String ___expandProjection(List<Object> columns, List<AParam> paramList) {
        List<String> cols = new ArrayList<>()
        if (columns == null || columns.isEmpty()) {
            return "*"
        }

        List<Object> finalCols = new LinkedList<>()
        for (c in columns) {
            if (c instanceof QResultProxy) {
                if (c.queryType != QueryType.PART) {
                    throw new NyException("Only query parts allowed to import within sql projection!")
                }
                List otherColumns = c.rawObject as List
                finalCols.addAll(otherColumns)
            } else if (c instanceof List) {
                finalCols.addAll(c);
            } else {
                finalCols.add(c)
            }
        }

        for (c in finalCols) {
            ___scanForParameters(c, paramList)

            if (c instanceof String) {
                cols.add(c)
            } else if (c instanceof Table) {
                String tbName = ___tableName((Table)c, QContextType.SELECT)
                cols.add("$tbName.*")
            } else if (c instanceof Column) {
                String cName = ___columnName(c, QContextType.SELECT)
                cols.add("$cName");
            } else {
                cols.add(String.valueOf(___resolve(c, QContextType.SELECT, paramList)))
            }
        }
        return cols.stream().collect(Collectors.joining(", "))
    }

    private def ___deriveSource(Table table, List<AParam> paramOrder, QContextType contextType) {
        if (table instanceof Join) {
            return ___tableJoinName(table, contextType, paramOrder)
        } else {
            if (table.__isResultOf()) {
                QResultProxy proxy = table.__resultOf as QResultProxy
                paramOrder.addAll(proxy.orderedParameters ?: [])
            }
            return ___tableName(table, contextType)
        }
    }

}