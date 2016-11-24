package com.virtusa.gto.nyql.db.mysql

import com.virtusa.gto.nyql.Case
import com.virtusa.gto.nyql.Column
import com.virtusa.gto.nyql.FunctionColumn
import com.virtusa.gto.nyql.Join
import com.virtusa.gto.nyql.QContextType
import com.virtusa.gto.nyql.QResultProxy
import com.virtusa.gto.nyql.QueryDelete
import com.virtusa.gto.nyql.QueryInsert
import com.virtusa.gto.nyql.QuerySelect
import com.virtusa.gto.nyql.QueryTruncate
import com.virtusa.gto.nyql.QueryUpdate
import com.virtusa.gto.nyql.StoredFunction
import com.virtusa.gto.nyql.Table
import com.virtusa.gto.nyql.Where
import com.virtusa.gto.nyql.db.QDdl
import com.virtusa.gto.nyql.db.QTranslator
import com.virtusa.gto.nyql.model.units.AParam
import com.virtusa.gto.nyql.utils.QUtils
import com.virtusa.gto.nyql.utils.QueryCombineType
import com.virtusa.gto.nyql.utils.QueryType
import groovy.transform.CompileStatic
/**
 * @author Isuru Weerarathna
 */
class MySql extends MySqlFunctions implements QTranslator {

    private static final MySqlDDL DDL = new MySqlDDL()

    static final String BACK_TICK = '`'
    static final String STR_QUOTE = '\"'

    private static final String NL = '\n'
    private static final String COMMA = ', '
    static final String OP = '('
    static final String CP = ')'
    private static final String _AS_ = ' AS '

    MySql() {}

    @CompileStatic
    @Override
    String ___ifColumn(Case aCaseCol, List<AParam> paramOrder) {
        if (aCaseCol.caseType == Case.CaseType.IFNULL) {
            StringBuilder query = new StringBuilder('IFNULL').append(OP)
            def whenCondition = aCaseCol.allConditions.get(0)
            Where.QCondition qCondition = (Where.QCondition) whenCondition._theCondition.clauses.get(0)
            query.append(___resolve(qCondition.leftOp, QContextType.SELECT, paramOrder))
            query.append(COMMA)
            query.append(___resolve(whenCondition._theResult, QContextType.SELECT, paramOrder))
            query.append(CP)

            if (aCaseCol.__aliasDefined()) {
                query.append(_AS_).append(QUtils.quoteIfWS(aCaseCol.__alias, BACK_TICK))
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
                query.append(_AS_).append(QUtils.quoteIfWS(aCaseCol.__alias, BACK_TICK))
            }
            return query.toString()
        }
    }

    @CompileStatic
    String JOIN(QContextType contextType) { 'JOIN' }

    @CompileStatic
    @Override
    String ___quoteString(final String text) {
        QUtils.quote(text, STR_QUOTE)
    }

    @CompileStatic
    @Override
    String ___convertBool(Boolean value) {
        value != null && value ? '1' : '0'
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
            return QUtils.quote(table.__name, BACK_TICK) + (table.__aliasDefined() ? ' ' + table.__alias : '')
        } else if (contextType == QContextType.SELECT || contextType == QContextType.INSERT_DATA || contextType == QContextType.UPDATE_SET) {
            if (table.__isResultOf()) {
                QResultProxy proxy = table.__resultOf as QResultProxy
                return QUtils.parenthesis(proxy.query.trim()) + (table.__aliasDefined() ? _AS_ + table.__alias : '')
            }
        }

        if (table.__aliasDefined()) {
            return table.__alias
        } else {
            return QUtils.quote(table.__name, BACK_TICK)
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
                return QUtils.quoteIfWS(column.__alias, BACK_TICK)
            }
        }

        if (column instanceof Case) {
            return ___ifColumn(column, paramList)
        }

        if (contextType == QContextType.INTO || contextType == QContextType.INSERT_PROJECTION) {
            return QUtils.quote(column.__name, BACK_TICK)
        }

        if (contextType == QContextType.DELETE_CONDITIONAL_JOIN) {
            if (column._owner.__aliasDefined()) {
                return QUtils.quoteIfWS(column._owner.__alias, BACK_TICK) + "." + QUtils.quoteIfWS(column.__name, BACK_TICK)
            }
            return QUtils.quote(column._owner.__name, BACK_TICK) + "." + QUtils.quoteIfWS(column.__name, BACK_TICK)
        } else if (contextType == QContextType.DELETE_CONDITIONAL) {
            return QUtils.quote(column._owner.__name, BACK_TICK) + "." + QUtils.quoteIfWS(column.__name, BACK_TICK)
        }

//        if (column.__aliasDefined() && contextType == QContextType.CONDITIONAL) {
//            return QUtils.quoteIfWS(column.__alias)
//        }

        if (column instanceof FunctionColumn) {
            return String.valueOf(this.invokeMethod(column._func, column._setOfCols ? column._columns : column._wrapper)) +
                    (column.__aliasDefined() ? _AS_ + QUtils.quoteIfWS(column.__alias, BACK_TICK) : '')
        } else {
            boolean tableHasAlias = column._owner != null && column._owner.__aliasDefined()
            if (tableHasAlias) {
                return column._owner.__alias + "." + column.__name +
                        (column.__aliasDefined() && contextType == QContextType.SELECT ?
                                _AS_ + QUtils.quoteIfWS(column.__alias, BACK_TICK) : '')
            } else {
                return QUtils.quoteIfWS(column.__name, BACK_TICK) +
                        (column.__aliasDefined() && contextType == QContextType.SELECT ?
                                _AS_ + QUtils.quoteIfWS(column.__alias, BACK_TICK) : '')
            }
        }
    }

    @CompileStatic
    @Override
    QResultProxy ___updateQuery(QueryUpdate q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()

        if (q._joiningTable != null) {
            // has joining tables
            query.append('UPDATE ').append(___deriveSource(q._joiningTable, paramList, QContextType.UPDATE_FROM)).append(' ').append(NL)
        } else {
            query.append('UPDATE ').append(___deriveSource(q.sourceTbl, paramList, QContextType.UPDATE_FROM)).append(' ').append(NL)
        }

        if (q._assigns != null && q._assigns.__hasAssignments()) {
            query.append('SET ').append(___expandAssignments(q._assigns, paramList, QContextType.UPDATE_SET)).append(' ').append(NL)
        }

        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append('WHERE ').append(___expandConditions(q.whereObj, paramList, QContextType.CONDITIONAL)).append(' ').append(NL)
        }

        new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.UPDATE)
    }

    @CompileStatic
    @Override
    QResultProxy ___storedFunction(StoredFunction sp) {
        StringBuilder query = new StringBuilder()
        query.append('{ CALL ').append(sp.name).append(OP)
        if (QUtils.notNullNorEmpty(sp.paramList)) {
            List<String> list = new LinkedList<>()
            for (AParam aParam : sp.paramList) {
                list.add('?')
            }
            query.append(list.join(COMMA))
        }
        query.append(CP).append(' }')

        return new QResultProxy(query: query.toString(), orderedParameters: sp.paramList,
                rawObject: sp, queryType: QueryType.DB_FUNCTION)
    }

    @CompileStatic
    QResultProxy ___combinationQuery(QueryCombineType combineType, List<Object> queries) {
        //if (combineType == QueryCombineType.INTERSECT) {
            //return manipulateIntersect(queries)
        //}

        String qStr
        if (combineType == QueryCombineType.UNION) {
            qStr = NL + ' UNION ALL ' + NL
        } else if (combineType == QueryCombineType.UNION_DISTINCT) {
            qStr = NL + ' UNION ' + NL
        } else {
            qStr = '; '
        }

        List<AParam> paramList = new LinkedList<>()
        StringJoiner joiner = new StringJoiner(qStr)
        for (Object q : queries) {
            if (q instanceof QResultProxy) {
                if (((QResultProxy)q).orderedParameters != null) {
                    paramList.addAll(((QResultProxy)q).orderedParameters)
                }
                joiner.add(QUtils.parenthesis(___resolve(q, QContextType.UNKNOWN)))
            } else {
                joiner.add(___resolve(q, QContextType.UNKNOWN, paramList))
            }
        }

        new QResultProxy(query: joiner.toString(), orderedParameters: paramList, queryType: QueryType.SELECT)
    }

    @CompileStatic
    QResultProxy ___deleteQuery(QueryDelete q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()
        Table mainTable =  q.sourceTbl
        QContextType delContext = QContextType.DELETE_CONDITIONAL

        query.append('DELETE ')
        if (q._joiningTable != null) {
            query.append(___deriveSource(mainTable, paramList, QContextType.DELETE_FROM_JOIN)).append(' ').append(NL)
            query.append('FROM ').append(___deriveSource(q._joiningTable, paramList, QContextType.DELETE_JOIN)).append(NL)
            delContext = QContextType.DELETE_CONDITIONAL_JOIN
        } else {
            query.append('FROM ').append(___deriveSource(mainTable, paramList, QContextType.DELETE_FROM)).append(NL)
        }

        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append(' WHERE ').append(___expandConditions(q.whereObj, paramList, delContext)).append(NL)
        }
        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.DELETE)
    }

    @CompileStatic
    QResultProxy ___selectQuery(QuerySelect q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()
        QueryType queryType = QueryType.SELECT
        if (q._intoTable != null) {
            queryType = QueryType.INSERT
            query.append('INSERT INTO ').append(___tableName(q._intoTable, QContextType.INTO)).append(' ')
            if (QUtils.notNullNorEmpty(q._intoColumns)) {
                query.append(QUtils.parenthesis(___expandProjection(q._intoColumns, paramList, QContextType.INSERT_PROJECTION)))
                        .append(' ')
            }
            query.append(NL)
        }

        query.append('SELECT ')
        if (q._distinct) {
            query.append('DISTINCT ')
        }
        query.append(___expandProjection(q.projection, paramList, QContextType.SELECT)).append(NL)
        // target is optional
        if (q._joiningTable ?: q.sourceTbl) {
            query.append(' FROM ').append(___deriveSource(q._joiningTable ?: q.sourceTbl, paramList, QContextType.FROM)).append(NL)
        }

        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append(' WHERE ').append(___expandConditions(q.whereObj, paramList, QContextType.CONDITIONAL)).append(NL)
        }

        if (QUtils.notNullNorEmpty(q.groupBy)) {
            String gClauses = QUtils.join(q.groupBy, { ___resolve(it, QContextType.GROUP_BY, paramList) }, COMMA, '', '')
            query.append(' GROUP BY ').append(gClauses)

            if (q.groupHaving != null) {
                query.append(NL).append(' HAVING ').append(___expandConditions(q.groupHaving, paramList, QContextType.HAVING))
            }
            query.append(NL)
        }

        if (QUtils.notNullNorEmpty(q.orderBy)) {
            String oClauses = QUtils.join(q.orderBy, { ___resolve(it, QContextType.ORDER_BY, paramList) }, COMMA, '', '')
            query.append(' ORDER BY ').append(oClauses).append(NL)
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

    @Override
    QResultProxy ___truncateQuery(QueryTruncate q) {
        StringBuilder query = new StringBuilder()
        query.append('TRUNCATE TABLE ').append(___tableName(q.sourceTbl, QContextType.TRUNCATE))

        new QResultProxy(query: query.toString(), orderedParameters: [], queryType: QueryType.TRUNCATE)
    }

    @CompileStatic
    QResultProxy ___insertQuery(QueryInsert q) {
        if (QUtils.isNullOrEmpty(q._data)) {
            return ___selectQuery(q)
        }

        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()

        query.append('INSERT INTO ').append(___resolve(q.sourceTbl, QContextType.INTO, paramList)).append(' (')
        List<String> colList = new LinkedList<>()
        List<String> valList = new LinkedList<>()

        for (Map.Entry<String, Object> entry : q._data) {
            colList.add(QUtils.quote(entry.key, BACK_TICK))

            if (entry.value instanceof AParam) {
                paramList.add((AParam)entry.value)
            } else if (entry.value instanceof Table) {
                appendParamsFromTable((Table)entry.value, paramList)
            }
            valList.add(String.valueOf(___resolve(entry.value, QContextType.INSERT_DATA, paramList)))
        }
        query.append(colList.join(COMMA))
                .append(') VALUES (')
                .append(valList.join(COMMA))
                .append(')')

        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.INSERT,
                returnType: q.returnType)
    }

    @CompileStatic
    @Override
    QDdl ___ddls() {
        DDL
    }

    /*
    private QResultProxy manipulateIntersect(List<Object> queries) {
        if (queries.size() != 2) {
            throw new NySyntaxException('MySQL intersect operator exactly requires only two queries!')
        }
        List<AParam> paramList = [] as Queue
        QResultProxy proxyTop = queries.get(0) as QResultProxy
        QResultProxy proxyDown = queries.get(1) as QResultProxy
        QuerySelect queryTop = (QuerySelect) proxyTop.qObject
        QuerySelect queryDown = (QuerySelect) proxyDown.qObject

        if (queryTop.projection == null || queryTop.projection.isEmpty()) {
            throw new NySyntaxException('MySQL intersect does not allow to have intersect on ALL fields!')
        }

        if (queryTop.projection.size() > 1) {
            // multiple expressions
            paramList.addAll(proxyTop.orderedParameters ?: [])
            paramList.addAll(proxyDown.orderedParameters ?: [])
            if (queryTop.whereObj == null) {
                queryTop.whereObj = new Where(queryTop._ctx)
            }
            if (queryTop.whereObj.__hasClauses()) {
                queryTop.whereObj.AND()
            }

            if (queryDown.whereObj == null) {
                queryDown.whereObj = new Where(queryDown._ctx)
            }

            for (int i = 0; i < queryDown.projection.size(); i++) {
                Column colDown = (Column) queryDown.projection.get(i)
                Column colUp = (Column) queryTop.projection.get(i)

                if (queryDown.whereObj.__hasClauses()) {
                    queryDown.whereObj.AND()
                }
                queryDown.whereObj.EQ(colDown, colUp)
            }
            queryDown.projection.clear()
            String qStr = ___resolve(queryTop, QContextType.UNKNOWN)
            return new QResultProxy(query: qStr, orderedParameters: paramList, queryType: QueryType.SELECT)

        } else {
            paramList.addAll(proxyTop.orderedParameters ?: [])
            paramList.addAll(proxyDown.orderedParameters ?: [])
            Column column = (Column) queryTop.projection.get(0)
            if (queryTop.whereObj == null) {
                queryTop.whereObj = new Where(queryTop._ctx)
            }

            if (queryTop.whereObj.__hasClauses()) {
                queryTop.whereObj.AND()
            }
            queryTop.whereObj.IN(column, proxyDown)

            String qStr = ___resolve(queryTop, QContextType.UNKNOWN)
            return new QResultProxy(query: qStr, orderedParameters: paramList, queryType: QueryType.SELECT)
        }
    }
    */
}