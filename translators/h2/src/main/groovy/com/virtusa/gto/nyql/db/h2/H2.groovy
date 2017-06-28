package com.virtusa.gto.nyql.db.h2

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
import com.virtusa.gto.nyql.db.TranslatorOptions
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.units.AParam
import com.virtusa.gto.nyql.utils.QUtils
import com.virtusa.gto.nyql.utils.QueryCombineType
import groovy.transform.CompileStatic

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
        value != null && value ? 'TRUE' : 'FALSE'
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
    QResultProxy ___deleteQuery(QueryDelete q) {
        return null
    }

    @Override
    QResultProxy ___combinationQuery(QueryCombineType combineType, List<Object> queries) {
        return null
    }

    @Override
    QResultProxy ___selectQuery(QuerySelect q) throws NyException {
        return null
    }

    @Override
    QResultProxy ___insertQuery(QueryInsert q) {
        return null
    }

    @Override
    QResultProxy ___storedFunction(StoredFunction sp) {
        return null
    }

    @Override
    QResultProxy ___updateQuery(QueryUpdate q) {
        return null
    }

    @Override
    QDdl ___ddls() {
        DDL
    }
}
