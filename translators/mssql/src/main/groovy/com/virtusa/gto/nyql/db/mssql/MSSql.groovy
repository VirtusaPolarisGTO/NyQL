package com.virtusa.gto.nyql.db.mssql

import com.virtusa.gto.nyql.Assign
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

import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * MS SQL Server translator.
 *
 * @author IWEERARATHNA
 */
class MSSql extends MSSqlFunctions implements QTranslator {

    private static final MSSqlDDL DDL = new MSSqlDDL()

    static final String QUOTE = '\"'
    static final String STR_QUOTE = "'"

    private static final String NL = '\n'

    @Override
    String ___ifColumn(Case aCaseCol, List<AParam> paramOrder) {
        if (aCaseCol.caseType == Case.CaseType.IFNULL) {
            StringBuilder query = new StringBuilder('ISNULL(')
            def whenCondition = aCaseCol.allConditions.get(0)
            Where.QCondition qCondition = (Where.QCondition) whenCondition._theCondition.clauses.get(0)
            query.append(___resolve(qCondition.leftOp, QContextType.SELECT, paramOrder))
            query.append(', ')
            query.append(___resolve(whenCondition._theResult, QContextType.SELECT, paramOrder))
            query.append(')')

            if (aCaseCol.__aliasDefined()) {
                query.append(' AS ').append(QUtils.quoteIfWS(aCaseCol.__alias, QUOTE))
            }
            return query.toString()

        } else {
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
                query.append(' AS ').append(QUtils.quoteIfWS(aCaseCol.__alias, QUOTE))
            }
            return query.toString()
        }
    }

    String JOIN(QContextType contextType) { 'INNER JOIN' }

    @Override
    String ___quoteString(final String text) {
        return QUtils.quote(text, STR_QUOTE)
    }

    @Override
    String ___convertBool(Boolean value) {
        return value != null && value ? '1' : '0'
    }

    @Override
    String ___tableName(final Table table, final QContextType contextType) {
        if (contextType == QContextType.DELETE_FROM || contextType == QContextType.DELETE_CONDITIONAL
                || contextType == QContextType.TRUNCATE) {
            return QUtils.quote(table.__name)
        } else if (contextType == QContextType.INTO) {
            return QUtils.quote(table.__name)
        } else if (contextType == QContextType.FROM) {
            if (table.__isResultOf()) {
                QResultProxy proxy = table.__resultOf as QResultProxy
                return '(' + proxy.query.trim() + ')' + (table.__aliasDefined() ? ' ' + table.__alias : '')
            }
            return QUtils.quote(table.__name, QUOTE) + (table.__aliasDefined() ? ' ' + table.__alias : '')
        } else {
            if (table.__aliasDefined()) {
                return table.__alias
            } else {
                return QUtils.quote(table.__name, QUOTE)
            }
        }
    }

    @Override
    String ___tableJoinName(final Join join, final QContextType contextType, List<AParam> paramOrder) {
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
            qstr.append(' ON ').append(___expandConditions(join.onConditions, paramOrder,
                    isInsideDelete(contextType) ? QContextType.DELETE_CONDITIONAL : QContextType.CONDITIONAL))
        }
        return qstr
    }

    private static boolean isInsideDelete(QContextType contextType) {
        return contextType == QContextType.DELETE_CONDITIONAL ||
                contextType == QContextType.DELETE_FROM
    }

    @Override
    String ___columnName(final Column column, final QContextType contextType) {
        if (contextType == QContextType.ORDER_BY || contextType == QContextType.GROUP_BY) {
            if (column.__aliasDefined()) {
                return QUtils.quoteIfWS(column.__alias, QUOTE)
            }
        }

        if (column instanceof Case) {
            return ___ifColumn(column, null)
        }

        if (contextType == QContextType.INTO || contextType == QContextType.INSERT_PROJECTION) {
            return QUtils.quote(column.__name, QUOTE)
        }

        if (contextType == QContextType.DELETE_CONDITIONAL) {
            return QUtils.quote(column._owner.__name, QUOTE) + "." + QUtils.quoteIfWS(column.__name, QUOTE)
        }

        if (column instanceof FunctionColumn) {
            return this.invokeMethod(column._func, column._setOfCols ? column._columns : column._wrapper) + (column.__aliasDefined() ?
                    ' AS ' + QUtils.quoteIfWS(column.__alias, QUOTE) : '')
        } else {
            boolean tableHasAlias = column._owner != null && column._owner.__aliasDefined()
            if (tableHasAlias) {
                return column._owner.__alias + "." + column.__name +
                        (column.__aliasDefined() && contextType == QContextType.SELECT ?
                                ' AS ' + QUtils.quoteIfWS(column.__alias, QUOTE) : '')
            } else {
                return QUtils.quoteIfWS(column.__name, QUOTE) +
                        (column.__aliasDefined() && contextType == QContextType.SELECT ?
                                ' AS ' + QUtils.quoteIfWS(column.__alias, QUOTE) : '')
            }
        }
    }

    @Override
    QResultProxy ___updateQuery(QueryUpdate q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()

        if (q._joiningTable != null) {
            // has joining tables
            query.append('UPDATE ').append(___deriveSource(q._joiningTable, paramList, QContextType.FROM)).append(' ').append(NL)
        } else {
            query.append('UPDATE ').append(___deriveSource(q.sourceTbl, paramList, QContextType.FROM)).append(' ').append(NL)
        }

        if (q._assigns.__hasAssignments()) {
            query.append('SET ').append(___expandAssignments(q._assigns, paramList, QContextType.CONDITIONAL)).append(' ').append(NL)
        }

        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append('WHERE ').append(___expandConditions(q.whereObj, paramList, QContextType.CONDITIONAL)).append(' ').append(NL)
        }

        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.UPDATE)
    }

    @Override
    QResultProxy ___storedFunction(StoredFunction sp) {
        StringBuilder query = new StringBuilder()
        query.append('{ CALL ').append(sp.name).append('(')
        if (QUtils.notNullNorEmpty(sp.paramList)) {
            query.append(sp.paramList.stream().map({ '?' }).collect(Collectors.joining(', ')))
        }
        query.append(') }')

        return new QResultProxy(query: query.toString(), orderedParameters: sp.paramList,
                rawObject: sp, queryType: QueryType.DB_FUNCTION)
    }

    QResultProxy ___combinationQuery(QueryCombineType combineType, List<Object> queries) {
        List<AParam> paramList = new LinkedList<>()
        Stream<Object> stream = queries.stream().map({ q ->
            if (q instanceof QResultProxy) {
                paramList.addAll(q.orderedParameters ?: [])
                return '(' + ___resolve(q, QContextType.UNKNOWN) + ')'
            } else {
                return ___resolve(q, QContextType.UNKNOWN, paramList)
            }
        });

        String qStr;
        if (combineType == QueryCombineType.UNION) {
            qStr = stream.collect(Collectors.joining(NL + 'UNION ALL' + NL))
        } else if (combineType == QueryCombineType.UNION_DISTINCT) {
            qStr = stream.collect(Collectors.joining(NL + 'UNION' + NL))
        } else if (combineType == QueryCombineType.INTERSECT) {
            qStr = stream.collect(Collectors.joining(NL + 'INTERSECT' + NL))
        } else {
            qStr = stream.collect(Collectors.joining('; '))
        }
        return new QResultProxy(query: qStr, orderedParameters: paramList, queryType: QueryType.SELECT)
    }

    QResultProxy ___deleteQuery(QueryDelete q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()
        Table mainTable =  q.sourceTbl

        query.append('DELETE ')
        if (q._joiningTable != null) {
            query.append(___deriveSource(mainTable, paramList, QContextType.DELETE_FROM)).append(' ').append(NL)
            query.append('FROM ').append(___deriveSource(q._joiningTable, paramList, QContextType.DELETE_FROM)).append(NL)
        } else {
            query.append('FROM ').append(___deriveSource(mainTable, paramList, QContextType.DELETE_FROM)).append(NL)
        }

        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append(' WHERE ').append(___expandConditions(q.whereObj, paramList, QContextType.DELETE_CONDITIONAL)).append(NL)
        }
        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.DELETE);
    }

    @Override
    QResultProxy ___truncateQuery(QueryTruncate q) {
        StringBuilder query = new StringBuilder()
        query.append('TRUNCATE TABLE ').append(___tableName(q.sourceTbl, QContextType.TRUNCATE))

        new QResultProxy(query: query.toString(), orderedParameters: [], queryType: QueryType.TRUNCATE)
    }

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
            query.append(___expandProjection(q._intoColumns, paramList, QContextType.INSERT_PROJECTION))
            return new QResultProxy(query: query.toString(), queryType: queryType,
                    orderedParameters: paramList, rawObject: q._intoColumns, qObject: q)
        }

        throw new NyException('Parts are no longer supports to reuse other than WHERE and JOINING!')
    }

    QResultProxy ___selectQuery(QuerySelect q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()
        QueryType queryType = QueryType.SELECT
        if (q._intoTable != null) {
            queryType = QueryType.INSERT
            query.append('INSERT INTO ').append(___tableName(q._intoTable, QContextType.INTO)).append(' ')
            if (QUtils.notNullNorEmpty(q._intoColumns)) {
                query.append('(')
                        .append(___expandProjection(q._intoColumns, paramList, QContextType.INSERT_PROJECTION))
                        .append(') ')
            }
            query.append(NL)
        }

        query.append('SELECT ');
        if (q._distinct) {
            query.append('DISTINCT ')
        }
        query.append(___expandProjection(q.projection, paramList)).append(NL)
        query.append(' FROM ').append(___deriveSource(q._joiningTable ?: q.sourceTbl, paramList, QContextType.FROM)).append(NL)

        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append(' WHERE ').append(___expandConditions(q.whereObj, paramList, QContextType.CONDITIONAL)).append(NL)
        }

        if (QUtils.notNullNorEmpty(q.groupBy)) {
            query.append(' GROUP BY ').append(q.groupBy.stream()
                    .map({ ___resolve(it, QContextType.GROUP_BY, paramList) })
                    .collect(Collectors.joining(', ')))

            if (q.groupHaving != null) {
                query.append(NL).append(' HAVING ').append(___expandConditions(q.groupHaving, paramList, QContextType.GROUP_BY))
            }
            query.append(NL)
        }

        if (QUtils.notNullNorEmpty(q.orderBy)) {
            query.append(' ORDER BY ').append(q.orderBy.stream()
                    .map({ ___resolve(it, QContextType.ORDER_BY, paramList) })
                    .collect(Collectors.joining(', ')))
                    .append(NL)
        }

        if (q._limit != null) {
            if (q._limit instanceof Integer && ((Integer) q._limit) > 0) {
                query.append(' LIMIT ').append(String.valueOf(q._limit)).append(NL)
            } else if (q._limit instanceof AParam) {
                paramList.add((AParam) q._limit)
                query.append(' LIMIT ').append(___resolve(q._limit, QContextType.ORDER_BY)).append(NL)
            }
        }

        if (q.offset != null) {
            if (q.offset instanceof Integer && ((Integer) q.offset) >= 0) {
                query.append(' OFFSET ').append(String.valueOf(q.offset)).append(NL)
            } else if (q.offset instanceof AParam) {
                paramList.add((AParam) q.offset)
                query.append(' OFFSET ').append(___resolve(q.offset, QContextType.ORDER_BY)).append(NL)
            }
        }

        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: queryType)
    }

    QResultProxy ___insertQuery(QueryInsert q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()

        query.append('INSERT INTO ').append(___resolve(q._targetTable, QContextType.INTO, paramList)).append(' (')
        List<String> colList = new LinkedList<>()
        List<String> valList = new LinkedList<>()
        q._data.each { k, v ->
            colList.add(QUtils.quote(k, QUOTE))

            if (v instanceof AParam) {
                paramList.add((AParam)v)
            }
            valList.add(String.valueOf(___resolve(v, QContextType.CONDITIONAL)))
        }
        query.append(colList.join(', '))
                .append(') VALUES (')
                .append(valList.join(', '))
                .append(')')

        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.INSERT, returnType: q.returnType)
    }

    @Override
    QDdl ___ddls() {
        return DDL
    }

    String ___expandProjection(List<Object> columns, List<AParam> paramList, QContextType contextType = QContextType.SELECT) {
        List<String> cols = new ArrayList<>()
        if (columns == null || columns.isEmpty()) {
            return '*'
        }

        List<Object> finalCols = new LinkedList<>()
        for (c in columns) {
            if (c instanceof QResultProxy) {
                if (c.queryType != QueryType.PART) {
                    throw new NyException('Only query parts allowed to import within sql projection!')
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
                String tbName = ___tableName((Table)c, contextType)
                cols.add("$tbName.*")
            } else if (c instanceof Column) {
                String cName = ___columnName(c, contextType)
                cols.add("$cName");
            } else {
                cols.add(String.valueOf(___resolve(c, contextType, paramList)))
            }
        }
        return cols.join(', ')
    }

    def ___deriveSource(Table table, List<AParam> paramOrder, QContextType contextType) {
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

    void ___expandColumn(Column column, List<AParam> paramList) {
        if (column instanceof FunctionColumn && column._columns != null) {
            column._columns.each {
                if (it instanceof FunctionColumn) {
                    ___expandColumn(it, paramList)
                } else if (it instanceof AParam) {
                    paramList.add(it)
                }
            }
        }
    }

    void ___scanForParameters(def expression, List<AParam> paramOrder) {
        if (expression instanceof AParam) {
            paramOrder?.add((AParam)expression)
        }
        if (expression instanceof QResultProxy) {
            QResultProxy resultProxy = expression
            paramOrder?.addAll(resultProxy.orderedParameters ?: [])
        }
        if (expression instanceof FunctionColumn) {
            ___expandColumn((FunctionColumn)expression, paramOrder)
        }
        if (expression instanceof List) {
            expression.each { ___scanForParameters(it, paramOrder) }
        }
    }

    String ___expandConditions(Where where, List<AParam> paramOrder, QContextType contextType=QContextType.UNKNOWN) {
        StringBuilder builder = new StringBuilder()
        List<Object> clauses = where.clauses
        for (c in clauses) {
            if (c instanceof String) {
                builder.append(c)
            } else if (c instanceof Where.QCondition) {
                builder.append(___expandCondition(c, paramOrder, contextType))
            } else if (c instanceof Where.QConditionGroup) {
                builder.append('(').append(___expandConditionGroup(c, paramOrder, contextType)).append(')')
            }
        }

        return builder.toString()
    }

    String ___expandCondition(Where.QCondition c, List<AParam> paramOrder, QContextType contextType) {
        if (c.leftOp instanceof AParam) {
            paramOrder?.add((AParam)c.leftOp)
        }
        ___scanForParameters(c.rightOp, paramOrder)
        boolean parenthesis = (c.rightOp instanceof QResultProxy)

        return ___resolve(c.leftOp, contextType) +
                (c.op.length() > 0 ? ' ' + String.valueOf(c.op) + ' ' : ' ') +
                (!parenthesis ? ___resolve(c.rightOp, contextType) : '(' + ___resolve(c.rightOp, contextType) + ')')
    }

    String ___expandConditionGroup(Where.QConditionGroup group, List<AParam> paramOrder, QContextType contextType) {
        String gCon = group.condConnector.isEmpty() ? '' : ' ' + group.condConnector + ' ';
        return group.where.clauses.stream()
                .map({ c -> if (c instanceof Where.QCondition) {
            return ___expandCondition(c, paramOrder, contextType)
        } else if (c instanceof Where.QConditionGroup) {
            return '(' + ___expandConditionGroup(c, paramOrder, contextType) + ')'
        } else {
            return String.valueOf(c)
        }
        }).collect(Collectors.joining(gCon))
    }

    String ___expandAssignments(Assign assign, List<AParam> paramOrder, QContextType contextType=QContextType.UNKNOWN) {
        List<Object> clauses = assign.assignments
        List<String> derived = new ArrayList<>()
        for (c in clauses) {
            if (c instanceof String) {
                derived.add(c)
            } else if (c instanceof Assign.AnAssign) {
                if (c.leftOp instanceof AParam) {
                    paramOrder.add((AParam)c.leftOp)
                }
                ___scanForParameters(c.rightOp, paramOrder)

                String val = ___resolve(c.leftOp, contextType, paramOrder) +
                        ' ' + c.op + ' ' + ___resolve(c.rightOp, contextType, paramOrder)
                derived.add(val)
            }
        }

        return derived.join(', ')
    }
}
