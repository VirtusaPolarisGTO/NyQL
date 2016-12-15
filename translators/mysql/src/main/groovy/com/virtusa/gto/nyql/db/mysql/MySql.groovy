package com.virtusa.gto.nyql.db.mysql

import com.virtusa.gto.nyql.*
import com.virtusa.gto.nyql.db.QDdl
import com.virtusa.gto.nyql.db.QTranslator
import com.virtusa.gto.nyql.db.TranslatorOptions
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

    MySql() { super() }

    MySql(TranslatorOptions theOptions) {
        super(theOptions)
    }

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

            query.append(columnAliasAs(aCaseCol, BACK_TICK))
            query.toString()

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

            query.append(columnAliasAs(aCaseCol, BACK_TICK))
            query.toString()
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
                return QUtils.parenthesis(proxy.query.trim()) + (table.__aliasDefined() ? ' ' + tableAlias(table, BACK_TICK) : '')
            }
            return QUtils.quote(table.__name, BACK_TICK) + (table.__aliasDefined() ? ' ' + tableAlias(table, BACK_TICK) : '')
        } else if (contextType == QContextType.SELECT || contextType == QContextType.INSERT_DATA || contextType == QContextType.UPDATE_SET) {
            if (table.__isResultOf()) {
                QResultProxy proxy = table.__resultOf as QResultProxy
                return QUtils.parenthesis(proxy.query.trim()) + tableAliasAs(table, BACK_TICK)
            }
        }

        if (table.__aliasDefined()) {
            return tableAlias(table, BACK_TICK)
        } else {
            return QUtils.quote(table.__name, BACK_TICK)
        }
    }

    @CompileStatic
    @Override
    String ___tableJoinName(final Join join, final QContextType contextType, List<AParam> paramOrder) {
        String jtype = invokeMethod(join.type, null)
        generateTableJoinName(join, jtype, contextType, paramOrder)
    }

    @CompileStatic
    @Override
    String ___columnName(final Column column, final QContextType contextType, List<AParam> paramList) {
        if (contextType == QContextType.ORDER_BY || contextType == QContextType.GROUP_BY || contextType == QContextType.HAVING) {
            if (column.__aliasDefined()) {
                return columnAlias(column, BACK_TICK)
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
                return tableAlias(column._owner, BACK_TICK) + "." + QUtils.quoteIfWS(column.__name, BACK_TICK)
            }
            return QUtils.quote(column._owner.__name, BACK_TICK) + "." + QUtils.quoteIfWS(column.__name, BACK_TICK)
        } else if (contextType == QContextType.DELETE_CONDITIONAL) {
            return QUtils.quote(column._owner.__name, BACK_TICK) + "." + QUtils.quoteIfWS(column.__name, BACK_TICK)
        }

        if (column instanceof FunctionColumn) {
            return String.valueOf(this.invokeMethod(column._func, column._setOfCols ? column._columns : column._wrapper)) +
                    columnAliasAs(column, BACK_TICK)
        } else {
            boolean tableHasAlias = column._owner != null && column._owner.__aliasDefined()
            if (tableHasAlias) {
                return tableAlias(column._owner, BACK_TICK) + "." + column.__name +
                        (column.__aliasDefined() && contextType == QContextType.SELECT ? columnAliasAs(column, BACK_TICK) : '')
            } else {
                return QUtils.quoteIfWS(column.__name, BACK_TICK) +
                        (column.__aliasDefined() && contextType == QContextType.SELECT ? columnAliasAs(column, BACK_TICK) : '')
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

        new QResultProxy(query: query.toString(), orderedParameters: sp.paramList,
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
        new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.DELETE)
    }

    @CompileStatic
    QResultProxy ___insertQuery(QueryInsert q) {
        generateInsertQuery(q, BACK_TICK)
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