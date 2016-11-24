package com.virtusa.gto.nyql.db.oracle

import com.virtusa.gto.nyql.Case
import com.virtusa.gto.nyql.Column
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
import com.virtusa.gto.nyql.db.QDdl
import com.virtusa.gto.nyql.db.QTranslator
import com.virtusa.gto.nyql.model.units.AParam
import com.virtusa.gto.nyql.utils.QUtils
import com.virtusa.gto.nyql.utils.QueryCombineType

/**
 * @author IWEERARATHNA
 */
abstract class OracleDb extends OracleFunctions implements QTranslator {

    static String STR_QUOTE = "'"

    @Override
    String ___ifColumn(Case aCaseCol, List<AParam> paramOrder) {
        return null
    }

    @Override
    String ___quoteString(String text) {
        return QUtils.quote(text, STR_QUOTE)
    }

    @Override
    String ___convertBool(Boolean value) {
        return null
    }

    @Override
    String ___tableName(Table table, QContextType contextType) {
        return null
    }

    @Override
    String ___tableJoinName(Join join, QContextType contextType, List<AParam> paramOrder) {
        return null
    }

    @Override
    String ___columnName(Column column, QContextType contextType, List<AParam> paramList) {
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
    QResultProxy ___truncateQuery(QueryTruncate q) {
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
