package com.virtusa.gto.nyql.db.h2

import com.virtusa.gto.nyql.CTE
import com.virtusa.gto.nyql.Case
import com.virtusa.gto.nyql.Column
import com.virtusa.gto.nyql.FunctionColumn
import com.virtusa.gto.nyql.Join
import com.virtusa.gto.nyql.QContextType
import com.virtusa.gto.nyql.QResultProxy
import com.virtusa.gto.nyql.QueryDelete
import com.virtusa.gto.nyql.QueryInsert
import com.virtusa.gto.nyql.QuerySelect
import com.virtusa.gto.nyql.QueryUpdate
import com.virtusa.gto.nyql.StoredFunction
import com.virtusa.gto.nyql.Table
import com.virtusa.gto.nyql.Where
import com.virtusa.gto.nyql.db.QDdl
import com.virtusa.gto.nyql.db.QTranslator
import com.virtusa.gto.nyql.db.SqlMisc
import com.virtusa.gto.nyql.db.TranslatorOptions
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QScriptList
import com.virtusa.gto.nyql.model.QSession
import com.virtusa.gto.nyql.model.units.AParam
import com.virtusa.gto.nyql.utils.QUtils
import com.virtusa.gto.nyql.utils.QueryCombineType
import com.virtusa.gto.nyql.utils.QueryType
import groovy.transform.CompileStatic

import java.util.stream.Collectors

/**
 * @author iweerarathna
 */
class H2 extends H2Functions implements QTranslator {

    private static final H2DDL DDL = new H2DDL()

    static final String BACK_TICK = '\"'
    static final String STR_QUOTE = '\''

    private static final String NL = '\n'
    private static final String COMMA = ', '
    static final String OP = '('
    static final String CP = ')'

    private static final String STR_TRUE = 'TRUE'
    private static final String STR_FALSE = 'FALSE'

    H2() {
        super()
    }

    H2(TranslatorOptions theOptions) {
        super(theOptions)
    }

    @Override
    protected String getQuoteChar() {
        BACK_TICK
    }

    @CompileStatic
    @Override
    String ___ifColumn(Case aCaseCol, List<AParam> paramOrder) {
        StringBuilder query = new StringBuilder()
        if (aCaseCol.caseType == Case.CaseType.IFNULL) {
            query.append('IFNULL').append(OP)
            def whenCondition = aCaseCol.allConditions.get(0)
            Where.QCondition qCondition = (Where.QCondition) whenCondition._theCondition.clauses.get(0)
            query.append(___resolve(qCondition.leftOp, QContextType.INSIDE_FUNCTION, paramOrder))
            query.append(COMMA)
            query.append(___resolve(whenCondition._theResult, QContextType.INSIDE_FUNCTION, paramOrder))
            query.append(CP)

        } else {
            List<Case.CaseCondition> conditions = aCaseCol.allConditions
            query.append(deriveCaseStr(aCaseCol, conditions, 0, paramOrder))
        }

        query.append(columnAliasAs(aCaseCol, BACK_TICK))
        query.toString()
    }

    @CompileStatic
    private String deriveCaseStr(Case aCase, List<Case.CaseCondition> conditionList, int currentIdx, List<AParam> paramOrder) {
        Case.CaseCondition cc = conditionList.get(currentIdx)
        StringBuilder q = new StringBuilder('CASEWHEN').append(OP)

        q.append(___expandConditions(cc._theCondition, paramOrder, QContextType.CONDITIONAL))
        q.append(COMMA)

        if (currentIdx < conditionList.size() - 1) {
            // everything before last item
            q.append(deriveCaseStr(aCase, conditionList, currentIdx + 1, paramOrder))

        } else {
            if (aCase.getElse() != null) {
                ___scanForParameters(aCase.getElse(), paramOrder);
                q.append(___resolve(aCase.getElse(), QContextType.INSIDE_FUNCTION, paramOrder))
            } else {
                q.append('NULL')
            }
        }
        return q.append(CP).toString()
    }

    @CompileStatic
    @Override
    String ___quoteString(String text) {
        QUtils.quote(text, STR_QUOTE)
    }

    @CompileStatic
    @Override
    String ___convertBool(Boolean value) {
        value != null && value ? STR_TRUE : STR_FALSE
    }

    @CompileStatic
    @Override
    String ___tableName(Table table, QContextType contextType) {
        if (contextType == QContextType.INTO || contextType == QContextType.TRUNCATE
                || contextType == QContextType.DELETE_FROM) {
            return QUtils.quote(table.__name)
        } else if (contextType == QContextType.FROM || contextType == QContextType.UPDATE_FROM
                || contextType == QContextType.DELETE_JOIN || contextType == QContextType.CONDITIONAL) {
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
    String ___tableJoinName(Join join, QContextType contextType, List<AParam> paramOrder) {
        String jtype = ___resolveJoinType(join.type)
        generateTableJoinName(join, jtype, contextType, paramOrder)
    }

    @CompileStatic
    @Override
    String ___columnName(Column column, QContextType contextType, List<AParam> paramList) {
        if (contextType == QContextType.ORDER_BY || contextType == QContextType.GROUP_BY || contextType == QContextType.HAVING) {
            if (column.__aliasDefined()) {
                return columnAlias(column, BACK_TICK)
            }
        }

        if (column instanceof Case) {
            return ___ifColumn((Case)column, paramList)
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
            return String.valueOf(
                    this.invokeMethod(column._func, [column._setOfCols ? column._columns : column._wrapper, paramList])) +
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

    @Override
    QResultProxy ___deleteQuery(QueryDelete q) throws NyException {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()
        Table mainTblClone = SqlMisc.cloneTable(q.sourceTbl, null)
        QContextType delContext = QContextType.DELETE_CONDITIONAL

        query.append('DELETE ')
        if (q._joiningTable != null) {
            if (QUtils.notNullNorEmpty(q.uniqueKeys)) {
                query.append('FROM ').append(___deriveSource(mainTblClone, paramList, QContextType.DELETE_FROM)).append(NL)

                String delCols = q.uniqueKeys.stream().map { it.__name }.collect(Collectors.joining(', '))

                QuerySelect tmpSel = new QuerySelect(q._ctx)
                String als = q.sourceTbl.__alias ?: q.sourceTbl.__name + '_tmpny'
                q.sourceTbl.__alias = als
                tmpSel.sourceTbl = q.sourceTbl
                tmpSel._joiningTable = q._joiningTable
                tmpSel.whereObj = q.whereObj
                String tmpFetch = q.uniqueKeys.stream()
                        .map { convertToAlias(als, BACK_TICK) + '.' + convertToAlias(it.__name, BACK_TICK) }
                        .collect(Collectors.joining(','))
                if (q.uniqueKeys.size() > 1) {
                    delCols = '(' + delCols + ')'
                    tmpFetch = '(' + tmpFetch + ')'
                }
                tmpSel.FETCH(tmpFetch)

                def querySel = ___selectQuery(tmpSel)

                Where where = new Where(q._ctx)
                where.IN(delCols, querySel)
                q.whereObj = where
                delContext = QContextType.CONDITIONAL

            } else {
                throw new NyException('H2 database does not support joins in DELETE queries! ' +
                        'Please write the query in alternative way, or use ON_UNIQUE_KEYS(...)')
            }

        } else {
            query.append('FROM ').append(___deriveSource(q.sourceTbl, paramList, QContextType.DELETE_FROM)).append(NL)
        }

        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append(' WHERE ').append(___expandConditions(q.whereObj, paramList, delContext)).append(NL)
        }
        new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.DELETE)
    }

    @Override
    QResultProxy ___combinationQuery(QueryCombineType combineType, List<Object> queries) {
        String qStr
        if (combineType == QueryCombineType.UNION) {
            qStr = NL + ' UNION ALL ' + NL
        } else if (combineType == QueryCombineType.UNION_DISTINCT) {
            qStr = NL + ' UNION ' + NL
        } else if (combineType == QueryCombineType.INTERSECT) {
            qStr = NL + ' INTERSECT ' + NL
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

            def px = _generateSelectQFullJoin(q)
            query.append(px.query)
            paramList.addAll(px.orderedParameters)
            return createProxy(query.toString(), queryType, paramList, null, null)

        } else {
            _generateSelectQFullJoin(q)
        }
    }

    @CompileStatic
    @Override
    QResultProxy ___insertQuery(QueryInsert q) {
        generateInsertQuery(q, BACK_TICK)
    }

    @CompileStatic
    @Override
    QResultProxy ___storedFunction(StoredFunction sp) {
        StringBuilder query = new StringBuilder()
        query.append('CALL ').append(sp.name).append(OP)
        if (QUtils.notNullNorEmpty(sp.paramList)) {
            List<String> list = new LinkedList<>()
            for (AParam aParam : sp.paramList) {
                list.add('?')
            }
            query.append(list.join(COMMA))
        }
        query.append(CP)

        new QResultProxy(query: query.toString(), orderedParameters: sp.paramList,
                rawObject: sp, queryType: QueryType.DB_FUNCTION)
    }

    @Override
    List<QResultProxy> ___cteQuery(CTE cte) {
        generateCTE(cte)
    }

    @Override
    QResultProxy ___updateQuery(QueryUpdate q) throws NyException {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()

        if (q._joiningTable != null) {
            // has joining tables
            // https://stackoverflow.com/questions/16309821/update-columns-in-multiple-tables-with-inner-join
            throw new NyException('H2 database does not support joins in UPDATE queries! Please write the query in alternative way.')
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

    @Override
    QDdl ___ddls() {
        DDL
    }

    @Override
    QScriptList getBootstrapScripts(QSession session) {
        QScriptList qScriptList = new QScriptList()

        qScriptList.scripts = [new QScript(
                id: '___alias_reverse',
                qSession: session,
                proxy: createProxy('CREATE ALIAS IF NOT EXISTS NYQL_REVERSE AS $$ ' +
                        'String reverse(String s) {' +
                        'return new StringBuilder(s).reverse().toString(); ' +
                        '} $$',
                        QueryType.INSERT, [], null, null))]

        qScriptList
    }

}
