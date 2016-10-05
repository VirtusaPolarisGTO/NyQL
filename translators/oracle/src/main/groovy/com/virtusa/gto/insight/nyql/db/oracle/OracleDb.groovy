package com.virtusa.gto.insight.nyql.db.oracle

import com.virtusa.gto.insight.nyql.Case
import com.virtusa.gto.insight.nyql.Column
import com.virtusa.gto.insight.nyql.Join
import com.virtusa.gto.insight.nyql.QContextType
import com.virtusa.gto.insight.nyql.QResultProxy
import com.virtusa.gto.insight.nyql.QueryDelete
import com.virtusa.gto.insight.nyql.QueryInsert
import com.virtusa.gto.insight.nyql.QueryPart
import com.virtusa.gto.insight.nyql.QuerySelect
import com.virtusa.gto.insight.nyql.QueryUpdate
import com.virtusa.gto.insight.nyql.StoredFunction
import com.virtusa.gto.insight.nyql.Table
import com.virtusa.gto.insight.nyql.db.QDdl
import com.virtusa.gto.insight.nyql.db.QTranslator
import com.virtusa.gto.insight.nyql.model.blocks.AParam
import com.virtusa.gto.insight.nyql.utils.QUtils
import com.virtusa.gto.insight.nyql.utils.QueryCombineType

/**
 * @author IWEERARATHNA
 */
class OracleDb implements QTranslator, OracleFunctions {

    static String STR_QUOTE = "'"

    @Override
    def ___ifColumn(Case aCaseCol, List<AParam> paramOrder) {
        return null
    }

    @Override
    def ___quoteString(String text) {
        return QUtils.quote(text, STR_QUOTE)
    }

    @Override
    def ___convertBool(Boolean value) {
        return null
    }

    @Override
    def ___tableName(Table table, QContextType contextType) {
        return null
    }

    @Override
    def ___tableJoinName(Join join, QContextType contextType, List<AParam> paramOrder) {
        return null
    }

    @Override
    def ___columnName(Column column, QContextType contextType) {
        return null
    }

    @Override
    QResultProxy ___partQuery(QueryPart q) {
        return null
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
    QResultProxy ___selectQuery(QuerySelect q) {
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
        return null
    }
}
