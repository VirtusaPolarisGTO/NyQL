package com.virtusa.gto.insight.nyql.db.mysql

import com.virtusa.gto.insight.nyql.*
import com.virtusa.gto.insight.nyql.db.QDdl
import com.virtusa.gto.insight.nyql.db.QTranslator
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.blocks.AParam
import com.virtusa.gto.insight.nyql.utils.QUtils
import com.virtusa.gto.insight.nyql.utils.QueryCombineType
import com.virtusa.gto.insight.nyql.utils.QueryType

import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * @author Isuru Weerarathna
 */
class MySql implements QTranslator, MySqlFunctions {

    private static final MySqlDDL DDL = new MySqlDDL()

    static final String BACK_TICK = '`'
    static final String STR_QUOTE = '\"'

    MySql() {}

    @Override
    def ___ifColumn(Case aCaseCol, List<AParam> paramOrder) {
        if (aCaseCol.caseType == Case.CaseType.IFNULL) {
            StringBuilder query = new StringBuilder('IFNULL(')
            def whenCondition = aCaseCol.allConditions.get(0)
            Where.QCondition qCondition = (Where.QCondition) whenCondition._theCondition.clauses.get(0)
            query.append(___resolve(qCondition.leftOp, QContextType.SELECT, paramOrder))
            query.append(', ')
            query.append(___resolve(whenCondition._theResult, QContextType.SELECT, paramOrder))
            query.append(')')

            if (aCaseCol.__aliasDefined()) {
                query.append(' AS ').append(QUtils.quoteIfWS(aCaseCol.__alias, BACK_TICK))
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
                query.append(' AS ').append(QUtils.quoteIfWS(aCaseCol.__alias, BACK_TICK))
            }
            return query.toString()
        }
    }

    String JOIN(QContextType contextType) { 'JOIN' }

    @Override
    def ___quoteString(final String text) {
        return QUtils.quote(text, STR_QUOTE)
    }

    @Override
    def ___convertBool(Boolean value) {
        return value != null && value ? '1' : '0'
    }

    @Override
    def ___tableName(final Table table, final QContextType contextType) {
        if (contextType == QContextType.INTO) {
            return QUtils.quote(table.__name)
        } else if (contextType == QContextType.FROM) {
            if (table.__isResultOf()) {
                QResultProxy proxy = table.__resultOf as QResultProxy
                return '(' + proxy.query.trim() + ')' + (table.__aliasDefined() ? ' ' + table.__alias : '')
            }
            return QUtils.quote(table.__name, BACK_TICK) + (table.__aliasDefined() ? ' ' + table.__alias : '')
        } else {
            if (table.__aliasDefined()) {
                return table.__alias
            } else {
                return QUtils.quote(table.__name, BACK_TICK)
            }
        }
    }

    @Override
    def ___tableJoinName(final Join join, final QContextType contextType, List<AParam> paramOrder) {
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
            qstr.append(' ON ').append(___expandConditions(join.onConditions, paramOrder, QContextType.CONDITIONAL))
        }
        return qstr
    }


    @Override
    def ___columnName(final Column column, final QContextType contextType) {
        if (contextType == QContextType.ORDER_BY || contextType == QContextType.GROUP_BY) {
            if (column.__aliasDefined()) {
                return QUtils.quoteIfWS(column.__alias, BACK_TICK)
            }
        }

        if (column instanceof Case) {
            return ___ifColumn(column, null)
        }

        if (contextType == QContextType.INTO) {
            return column.__name
        }

        if (column instanceof FunctionColumn) {
            return this.invokeMethod(column._func, column._setOfCols ? column._columns : column._wrapper) + (column.__aliasDefined() ?
                    ' AS ' + QUtils.quoteIfWS(column.__alias, BACK_TICK) : '')
        } else {
            boolean tableHasAlias = column._owner != null && column._owner.__aliasDefined()
            if (tableHasAlias) {
                return column._owner.__alias + "." + column.__name +
                        (column.__aliasDefined() && contextType == QContextType.SELECT ?
                                ' AS ' + QUtils.quoteIfWS(column.__alias, BACK_TICK) : '')
            } else {
                return QUtils.quoteIfWS(column.__name, BACK_TICK) +
                        (column.__aliasDefined() && contextType == QContextType.SELECT ?
                                ' AS ' + QUtils.quoteIfWS(column.__alias, BACK_TICK) : '')
            }
        }
    }

    @Override
    QResultProxy ___updateQuery(QueryUpdate q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()

        if (q._joiningTable != null) {
            // has joining tables
            query.append('UPDATE ').append(___deriveSource(q._joiningTable, paramList, QContextType.FROM)).append(' \n')
        } else {
            query.append('UPDATE ').append(___deriveSource(q.sourceTbl, paramList, QContextType.FROM)).append(' \n')
        }

        if (q._assigns.__hasAssignments()) {
            query.append('SET ').append(___expandAssignments(q._assigns, paramList, QContextType.CONDITIONAL)).append(' \n')
        }

        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append('WHERE ').append(___expandConditions(q.whereObj, paramList, QContextType.CONDITIONAL)).append(' \n')
        }

        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.UPDATE, qObject: q)
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
            qStr = stream.collect(Collectors.joining('\nUNION ALL\n'))
        } else if (combineType == QueryCombineType.UNION_DISTINCT) {
            qStr = stream.collect(Collectors.joining('\nUNION\n'))
        } else {
            qStr = stream.collect(Collectors.joining('; '))
        }
        return new QResultProxy(query: qStr, orderedParameters: paramList, queryType: QueryType.SELECT, qObject: queries)
    }

    QResultProxy ___deleteQuery(QueryDelete q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()
        query.append('DELETE FROM ').append(___deriveSource(q.sourceTbl, paramList, QContextType.FROM)).append('\n')
        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append(' WHERE ').append(___expandConditions(q.whereObj, paramList, QContextType.CONDITIONAL)).append('\n')
        }
        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.DELETE);
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
                query.append(q._intoColumns.stream().map({
                    return ___columnName(it, QContextType.INTO)
                }).collect(Collectors.joining(', ', '(', ')')))
            }
            query.append("\n")
        }

        query.append('SELECT ');
        if (q._distinct) {
            query.append('DISTINCT ')
        }
        query.append(___expandProjection(q.projection, paramList)).append("\n")
        query.append(' FROM ').append(___deriveSource(q._joiningTable ?: q.sourceTbl, paramList, QContextType.FROM)).append('\n')

        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append(' WHERE ').append(___expandConditions(q.whereObj, paramList, QContextType.CONDITIONAL)).append('\n')
        }

        if (QUtils.notNullNorEmpty(q.groupBy)) {
            query.append(' GROUP BY ').append(q.groupBy.stream()
                    .map({ return ___columnName(it, QContextType.GROUP_BY) })
                    .collect(Collectors.joining(', ')))

            if (q.groupHaving != null) {
                query.append('\n').append(' HAVING ').append(___expandConditions(q.groupHaving, paramList, QContextType.GROUP_BY))
            }
            query.append('\n')
        }

        if (QUtils.notNullNorEmpty(q.orderBy)) {
            query.append(' ORDER BY ').append(q.orderBy.stream()
                    .map({ t -> return ___columnName(t, QContextType.ORDER_BY) })
                    .collect(Collectors.joining(', ')))
                    .append('\n')
        }

        if (q._limit != null) {
            if (q._limit instanceof Integer && ((Integer) q._limit) > 0) {
                query.append(' LIMIT ').append(String.valueOf(q._limit)).append('\n')
            } else if (q._limit instanceof AParam) {
                paramList.add((AParam) q._limit)
                query.append(' LIMIT ').append(___resolve(q._limit, QContextType.ORDER_BY)).append('\n')
            }
        }

        if (q.offset != null) {
            if (q.offset instanceof Integer && ((Integer) q.offset) >= 0) {
                query.append(' OFFSET ').append(String.valueOf(q.offset)).append('\n')
            } else if (q.offset instanceof AParam) {
                paramList.add((AParam) q.offset)
                query.append(' OFFSET ').append(___resolve(q.offset, QContextType.ORDER_BY)).append('\n')
            }
        }

        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: queryType, qObject: q)
    }

    QResultProxy ___insertQuery(QueryInsert q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()

        query.append('INSERT INTO ').append(___resolve(q._targetTable, QContextType.INTO, paramList)).append(' (')
        List<String> colList = new LinkedList<>()
        List<String> valList = new LinkedList<>()
        q._data.each { k, v ->
            colList.add(k)

            if (v instanceof AParam) {
                paramList.add((AParam)v)
            }
            valList.add(String.valueOf(___resolve(v, QContextType.CONDITIONAL)))
        }
        query.append(colList.stream().collect(Collectors.joining(', ')))
                .append(') VALUES (')
                .append(valList.stream().collect(Collectors.joining(', ')))
                .append(')')

        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.INSERT, qObject: q)
    }

    @Override
    QDdl ___ddls() {
        return DDL
    }

    String ___expandProjection(List<Object> columns, List<AParam> paramList) {
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
                String tbName = ___tableName((Table)c, QContextType.SELECT)
                cols.add("$tbName.*")
            } else if (c instanceof Column) {
                String cName = ___columnName(c, QContextType.SELECT)
                cols.add("$cName");
            } else {
                cols.add(String.valueOf(___resolve(c, QContextType.SELECT, paramList)))
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
                (c.op.length() > 0 ? ' ' + c.op + ' ' : ' ') +
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

                derived.add(___resolve(c.leftOp, QContextType.CONDITIONAL, paramOrder) +
                        ' ' + c.op + ' ' + ___resolve(c.rightOp, QContextType.CONDITIONAL, paramOrder))
            }
        }

        return derived.join(', ')
    }

}