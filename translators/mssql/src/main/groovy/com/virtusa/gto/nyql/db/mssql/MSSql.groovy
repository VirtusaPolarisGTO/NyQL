package com.virtusa.gto.nyql.db.mssql

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
 * MS SQL Server translator.
 *
 * @author IWEERARATHNA
 */
class MSSql extends MSSqlFunctions implements QTranslator {

    private static final MSSqlDDL DDL = new MSSqlDDL()

    static final String QUOTE = '\"'
    static final String STR_QUOTE = "'"
    private static final String COMMA = ', '
    static final String OP = '('
    static final String CP = ')'

    private static final String NL = '\n'

    MSSql() {
    }

    MSSql(TranslatorOptions theOptions) {
        super(theOptions)
    }

    @CompileStatic
    @Override
    String ___ifColumn(Case aCaseCol, List<AParam> paramOrder) {
        if (aCaseCol.caseType == Case.CaseType.IFNULL) {
            StringBuilder query = new StringBuilder('ISNULL').append(OP)
            def whenCondition = aCaseCol.allConditions.get(0)
            Where.QCondition qCondition = (Where.QCondition) whenCondition._theCondition.clauses.get(0)
            query.append(___resolve(qCondition.leftOp, QContextType.SELECT, paramOrder))
            query.append(COMMA)
            query.append(___resolve(whenCondition._theResult, QContextType.SELECT, paramOrder))
            query.append(CP)

            query.append(columnAliasAs(aCaseCol, QUOTE))
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

            query.append(columnAliasAs(aCaseCol, QUOTE))
            query.toString()
        }
    }

    @CompileStatic
    String JOIN(QContextType contextType) { 'INNER JOIN' }

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
            return QUtils.quote(table.__name, QUOTE)
        } else if (contextType == QContextType.FROM || contextType == QContextType.UPDATE_FROM
                || contextType == QContextType.DELETE_JOIN) {
            if (table.__isResultOf()) {
                QResultProxy proxy = table.__resultOf as QResultProxy
                return QUtils.parenthesis(proxy.query.trim()) + (table.__aliasDefined() ? ' ' + tableAlias(table, QUOTE) : '')
            }
            return QUtils.quote(table.__name, QUOTE) + (table.__aliasDefined() ? ' ' + tableAlias(table, QUOTE) : '')
        } else if (contextType == QContextType.SELECT || contextType == QContextType.INSERT_DATA || contextType == QContextType.UPDATE_SET) {
            if (table.__isResultOf()) {
                QResultProxy proxy = table.__resultOf as QResultProxy
                return QUtils.parenthesis(proxy.query.trim()) + tableAliasAs(table, QUOTE)
            }
        }

        if (table.__aliasDefined()) {
            return tableAlias(table, QUOTE)
        } else {
            return QUtils.quote(table.__name, QUOTE)
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
                return columnAlias(column, QUOTE)
            }
        }

        if (column instanceof Case) {
            return ___ifColumn(column, paramList)
        }

        if (contextType == QContextType.INTO || contextType == QContextType.INSERT_PROJECTION) {
            return QUtils.quote(column.__name, QUOTE)
        }

        if (contextType == QContextType.DELETE_CONDITIONAL_JOIN) {
            if (column._owner.__aliasDefined()) {
                return tableAlias(column._owner, QUOTE) + "." + QUtils.quoteIfWS(column.__name, QUOTE)
            }
            return QUtils.quote(column._owner.__name, QUOTE) + "." + QUtils.quoteIfWS(column.__name, QUOTE)
        } else if (contextType == QContextType.DELETE_CONDITIONAL) {
            return QUtils.quote(column._owner.__name, QUOTE) + "." + QUtils.quoteIfWS(column.__name, QUOTE)
        }

        if (column instanceof FunctionColumn) {
            return String.valueOf(this.invokeMethod(column._func, column._setOfCols ? column._columns : column._wrapper)) +
                    columnAliasAs(column, QUOTE)
        } else {
            boolean tableHasAlias = column._owner != null && column._owner.__aliasDefined()
            if (tableHasAlias) {
                return tableAlias(column._owner, QUOTE) + "." + column.__name +
                        (column.__aliasDefined() && contextType == QContextType.SELECT ? columnAliasAs(column, QUOTE) : '')
            } else {
                return QUtils.quoteIfWS(column.__name, QUOTE) +
                        (column.__aliasDefined() && contextType == QContextType.SELECT ? columnAliasAs(column, QUOTE) : '')
            }
        }
    }

    /**
     * UPDATE im
     SET mf_item_number = gm.SKU --etc
     FROM item_master im
     JOIN group_master gm
     ON im.sku = gm.sku
     JOIN Manufacturer_Master mm
     ON gm.ManufacturerID = mm.ManufacturerID
     WHERE im.mf_item_number like 'STA%' AND
     gm.manufacturerID = 34


     Ex2:

     UPDATE  T2
     SET    T2. Name = T1 .Name
     FROM   Table2 as T2 INNER JOIN Table1 as T1
     ON     T1. Id = T1 .Id;
     * @param q
     * @return
     */
    @CompileStatic
    @Override
    QResultProxy ___updateQuery(QueryUpdate q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()

        query.append('UPDATE ')
        if (q._joiningTable != null) {
            query.append(___deriveSource(q.sourceTbl, paramList, QContextType.UPDATE_FROM_JOIN))
        } else {
            query.append(___deriveSource(q.sourceTbl, paramList, QContextType.UPDATE_FROM))
        }

        if (q._assigns != null && q._assigns.__hasAssignments()) {
            query.append(' SET ').append(___expandAssignments(q._assigns, paramList, QContextType.UPDATE_SET)).append(NL)
        }

        if (q._joiningTable != null) {
            query.append(' FROM ').append(___deriveSource(q._joiningTable, paramList, QContextType.UPDATE_JOIN))
        }

        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append(' WHERE ').append(___expandConditions(q.whereObj, paramList, QContextType.CONDITIONAL))
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
    @Override
    QResultProxy ___combinationQuery(QueryCombineType combineType, List<Object> queries) {
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

    /**
     * DELETE  T2
     FROM   Table2 as T2 INNER JOIN Table1 as T1
     ON     T1. Id = T1 .Id;
     * @param q
     * @return
     */
    @CompileStatic
    @Override
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

    QResultProxy ___insertQuery(QueryInsert q) {
        generateInsertQuery(q, QUOTE)
    }

    @Override
    QDdl ___ddls() {
        return DDL
    }

}
