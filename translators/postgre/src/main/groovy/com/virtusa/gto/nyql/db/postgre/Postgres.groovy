package com.virtusa.gto.nyql.db.postgre

import com.virtusa.gto.nyql.*
import com.virtusa.gto.nyql.db.QDdl
import com.virtusa.gto.nyql.db.QTranslator
import com.virtusa.gto.nyql.db.TranslatorOptions
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.exceptions.NySyntaxException
import com.virtusa.gto.nyql.model.DbInfo
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

    private static String TRUE_STR = 't'
    private static String FALSE_STR = 'f'

    private DbInfo dbInfo

    Postgres() { super() }

    Postgres(TranslatorOptions theOptions, DbInfo theDbInfo) {
        super(theOptions)
        dbInfo = theDbInfo
    }

    @Override
    String greatest(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)

        if (c instanceof List) {
            return 'GREATEST' + ___resolveIn(c, pmx)
        } else {
            throw new NySyntaxException('GREATEST function requires at least two or more values!')
        }
    }

    @Override
    String least(Object cx) {
        def c = ___val(cx)
        def pmx = ___pm(cx)

        if (c instanceof List) {
            return 'LEAST' + ___resolveIn(c, pmx)
        } else {
            throw new NySyntaxException('LEAST function requires at least two or more values!')
        }
    }

    @CompileStatic
    @Override
    String ___ifColumn(Case aCaseCol, List<AParam> paramOrder) {
        StringBuilder query = new StringBuilder('CASE')
        List<Case.CaseCondition> conditions = aCaseCol.allConditions
        for (Case.CaseCondition cc : conditions) {
            query.append(' WHEN ').append(___expandConditions(cc._theCondition, paramOrder, QContextType.CONDITIONAL))
            query.append(' THEN ').append(___resolve(cc._theResult, QContextType.INSIDE_FUNCTION))
        }

        if (aCaseCol.getElse() != null) {
            query.append(' ELSE ').append(___resolve(aCaseCol.getElse(), QContextType.INSIDE_FUNCTION))
        }
        query.append(' END')

        query.append(columnAliasAs(aCaseCol, DOUBLE_QUOTE))
        query.toString()
    }

    @CompileStatic
    @Override
    String ___quoteString(final String text) {
        QUtils.quote(text, STR_QUOTE)
    }

    @CompileStatic
    @Override
    String ___convertBool(Boolean value) {
        ___quoteString(value ? TRUE_STR : FALSE_STR)
    }

    @CompileStatic
    @Override
    String ___tableName(final Table table, final QContextType contextType) {
        if (contextType == QContextType.INTO || contextType == QContextType.TRUNCATE
                || contextType == QContextType.DELETE_FROM) {
            return QUtils.quote(table.__name, DOUBLE_QUOTE)
        } else if (contextType == QContextType.FROM || contextType == QContextType.UPDATE_FROM
                || contextType == QContextType.DELETE_JOIN || contextType == QContextType.CONDITIONAL) {
            if (table.__isResultOf()) {
                QResultProxy proxy = table.__resultOf as QResultProxy
                return QUtils.parenthesis(proxy.query.trim()) + (table.__aliasDefined() ? ' ' + tableAlias(table, DOUBLE_QUOTE) : '')
            }
            return QUtils.quote(table.__name, DOUBLE_QUOTE) + (table.__aliasDefined() ? ' ' + tableAlias(table, DOUBLE_QUOTE) : '')
        } else if (contextType == QContextType.SELECT || contextType == QContextType.INSERT_DATA || contextType == QContextType.UPDATE_SET) {
            if (table.__isResultOf()) {
                QResultProxy proxy = table.__resultOf as QResultProxy
                return QUtils.parenthesis(proxy.query.trim()) + tableAliasAs(table, DOUBLE_QUOTE)
            }
        }

        if (table.__aliasDefined()) {
            return tableAlias(table, DOUBLE_QUOTE)
        } else {
            return QUtils.quote(table.__name, DOUBLE_QUOTE)
        }
    }

    @Override
    String ___tableJoinName(Join join, QContextType contextType, List<AParam> paramOrder) {
        String jtype = invokeMethod(join.type, null)
        generateTableJoinName(join, jtype, contextType, paramOrder)
    }

    @CompileStatic
    @Override
    String ___columnName(final Column column, final QContextType contextType, List<AParam> paramList) {
        if (contextType == QContextType.ORDER_BY || contextType == QContextType.GROUP_BY || contextType == QContextType.HAVING) {
            if (column.__aliasDefined()) {
                return columnAlias(column, DOUBLE_QUOTE)
            }
        }

        if (column instanceof Case) {
            return ___ifColumn(column, paramList)
        }

        if (contextType == QContextType.INTO || contextType == QContextType.INSERT_PROJECTION) {
            return QUtils.quote(column.__name, DOUBLE_QUOTE)
        }

        if (contextType == QContextType.DELETE_CONDITIONAL_JOIN) {
            if (column._owner.__aliasDefined()) {
                return tableAlias(column._owner, DOUBLE_QUOTE) + "." + QUtils.quoteIfWS(column.__name, DOUBLE_QUOTE)
            }
            return QUtils.quote(column._owner.__name, DOUBLE_QUOTE) + "." + QUtils.quoteIfWS(column.__name, DOUBLE_QUOTE)
        } else if (contextType == QContextType.DELETE_CONDITIONAL) {
            return QUtils.quote(column._owner.__name, DOUBLE_QUOTE) + "." + QUtils.quoteIfWS(column.__name, DOUBLE_QUOTE)
        }

        if (column instanceof FunctionColumn) {
            return String.valueOf(this.invokeMethod(column._func, column._setOfCols ? column._columns : column._wrapper)) +
                    columnAliasAs(column, DOUBLE_QUOTE)
        } else {
            boolean tableHasAlias = column._owner != null && column._owner.__aliasDefined()
            if (tableHasAlias) {
                return tableAlias(column._owner, DOUBLE_QUOTE) + "." + column.__name +
                        (column.__aliasDefined() && contextType == QContextType.SELECT ? columnAliasAs(column, DOUBLE_QUOTE) : '')
            } else {
                return QUtils.quoteIfWS(column.__name, DOUBLE_QUOTE) +
                        (column.__aliasDefined() && contextType == QContextType.SELECT ? columnAliasAs(column, DOUBLE_QUOTE) : '')
            }
        }
    }

    @Override
    QResultProxy ___selectQuery(QuerySelect q) throws NyException {
        if (q.get_intoTable() != null) {
            List<AParam> paramList = new LinkedList<>()
            StringBuilder query = new StringBuilder()
            QueryType queryType = QueryType.INSERT

            if (q._intoTemp) {
                query.append('CREATE TEMPORARY TABLE ').append(___tableName(q.get_intoTable(), QContextType.INTO)).append(' ')
            } else {
                query.append('INSERT INTO ').append(___tableName(q.get_intoTable(), QContextType.INTO)).append(' ');
            }

            // append column names...
            if (QUtils.notNullNorEmpty(q.get_intoColumns())) {
                query.append(QUtils.parenthesis(___expandProjection(q.get_intoColumns(), paramList, QContextType.INSERT_PROJECTION)))
                        .append(' ')
            }

            if (q._intoTemp) {
                query.append('AS ')
            }
            query.append(NL)

            def px = generateSelectQueryBody(q, paramList)
            query.append(px.toString())
            return createProxy(query.toString(), queryType, paramList, null, null)

        } else {
            List<AParam> paramList = new LinkedList<>()
            StringBuilder query = new StringBuilder()

            query.append(generateSelectQueryBody(q, paramList).toString())
            return createProxy(query.toString(), QueryType.SELECT, paramList, null, null)
        }
    }

    @Override
    QResultProxy ___deleteQuery(QueryDelete q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()
        query.append('DELETE FROM ').append(___deriveSource(q.sourceTbl, paramList, QContextType.UPDATE_FROM)).append('\n')
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
            qStr = stream.collect(Collectors.joining("\n UNION ALL \n"))
        } else if (combineType == QueryCombineType.UNION_DISTINCT) {
            qStr = stream.collect(Collectors.joining("\n UNION \n"))
        } else if (combineType == QueryCombineType.INTERSECT) {
            qStr = stream.collect(Collectors.joining(NL + ' INTERSECT ' + NL))
        } else {
            qStr = stream.collect(Collectors.joining("; "))
        }
        new QResultProxy(query: qStr, orderedParameters: paramList, queryType: QueryType.SELECT)
    }

    @Override
    QResultProxy ___insertQuery(QueryInsert q) {
        generateInsertQuery(q, DOUBLE_QUOTE)
    }

    @Override
    protected void ___selectQueryGroupByClause(QuerySelect q, StringBuilder query, List<AParam> paramList) throws NyException {
        String gClauses = QUtils.join(q.getGroupBy(), { ___resolve(it, QContextType.GROUP_BY, paramList) }, COMMA, "", "");
        if (q.groupByRollup) {
            query.append(" GROUP BY ROLLUP(").append(gClauses).append(")");
        } else {
            query.append(" GROUP BY ").append(gClauses);
        }
    }

    @Override
    QResultProxy ___storedFunction(StoredFunction sp) {
        StringBuilder query = new StringBuilder()
        query.append('{ ? = CALL ').append(sp.name).append('( ')
        if (QUtils.notNullNorEmpty(sp.paramList)) {
            query.append(sp.paramList.stream().map({ '?' }).collect(Collectors.joining(COMMA)))
        }
        query.append(' ) }')

        new QResultProxy(query: query.toString(), orderedParameters: sp.paramList, rawObject: sp, queryType: QueryType.DB_FUNCTION)
    }

    @Override
    QResultProxy ___updateQuery(QueryUpdate q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()
        query.append('UPDATE ').append(___deriveSource(q.sourceTbl, paramList, QContextType.UPDATE_FROM)).append(" \n")
        if (q._assigns.__hasAssignments()) {
            query.append('SET ').append(___expandAssignments(q._assigns, paramList, QContextType.UPDATE_SET)).append(" \n")
        }

        if (q._joiningTable != null) {
            // has joining tables
            List<Table> allTables = new ArrayList<>()
            List<String> allClauses = new ArrayList<>()
            QUtils.filterAllJoinConditions(q._joiningTable, allClauses, ' AND ')
            QUtils.findAlTables(q._joiningTable, allTables)
            allTables.remove(q.sourceTbl)

            query.append('FROM ').append(allTables.stream().map({
                        t -> return ___tableName(t, QContextType.UPDATE_FROM)
                    }).collect(Collectors.joining(", "))).append("\n")

            if (q.whereObj == null) {
                q.whereObj = new Where(q._ctx)
            } else if (!q.whereObj.clauses.isEmpty() && !allClauses.isEmpty()) {
                q.whereObj.clauses.add(0, ' AND ')
            }
            q.whereObj.clauses.addAll(0, allClauses)
        }


        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append('WHERE ').append(___expandConditions(q.whereObj, paramList, QContextType.CONDITIONAL)).append("\n")
        }

        new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.UPDATE)
    }

    @Override
    QDdl ___ddls() {
        DDL
    }

    @Override
    List<QResultProxy> ___cteQuery(CTE cte) {
        generateCTE(cte)
    }

    @Override
    protected String getQuoteChar() {
        DOUBLE_QUOTE
    }
}