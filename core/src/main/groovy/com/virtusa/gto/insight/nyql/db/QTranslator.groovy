package com.virtusa.gto.insight.nyql.db

import com.virtusa.gto.insight.nyql.*
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.blocks.AParam
import com.virtusa.gto.insight.nyql.model.blocks.ParamList
import com.virtusa.gto.insight.nyql.model.blocks.QNumber
import com.virtusa.gto.insight.nyql.model.blocks.QString
import com.virtusa.gto.insight.nyql.utils.QueryCombineType

import java.util.stream.Collectors

/**
 * @author Isuru Weerarathna
 */
trait QTranslator extends QJoins {

    String NULL() { "NULL" }
    String COMPARATOR_NULL() { "IS" }

    def ___resolve(Object obj, QContextType contextType, List<AParam> paramOrder=null) {
        if (obj == null) {
            return NULL()
        }

        if (obj instanceof QString) {
            return ___quoteString(obj.text) + (obj.__aliasDefined() && contextType == QContextType.SELECT ? " AS " + obj.__alias : "")
        } else if (obj instanceof QNumber) {
            return ___convertNumeric(obj.number) + (obj.__aliasDefined() && contextType == QContextType.SELECT ? " AS " + obj.__alias : "")
        } else if (obj instanceof Join) {
            return ___tableJoinName(obj, contextType, paramOrder)
        } else if (obj instanceof Table) {
            return ___tableName(obj, contextType)
        } else if (obj instanceof Column) {
            return ___columnName(obj, contextType)
        } else if (obj instanceof Boolean) {
            return ___convertBool(obj)
        } else if (obj instanceof String || obj instanceof GString) {
            return String.valueOf(obj)
        } else if (obj instanceof Number) {
            return ___convertNumeric(obj)
        } else if (obj instanceof AParam) {
            if (obj instanceof ParamList) {
                return "::" + obj.__name + "::"
            }
            return "?" + (obj.__aliasDefined() && contextType == QContextType.SELECT ? " AS " + obj.__alias : "")
        } else if (obj instanceof QResultProxy) {
            return (obj.query ?: "").trim()
        } else if (obj instanceof List) {
            return obj.stream().map({ ___resolve(it, contextType, paramOrder) }).collect(Collectors.joining(", ", "(", ")"))
        } else {
            throw new NyException("Unsupported data object to convert! [" + obj + ", type: " + obj.class + "]")
        }
    }

    abstract def ___ifColumn(Case aCaseCol, List<AParam> paramOrder)

    abstract def ___quoteString(String text)

    abstract def ___convertBool(Boolean value)

    abstract def ___tableName(Table table, QContextType contextType)

    abstract def ___tableJoinName(Join join, QContextType contextType, List<AParam> paramOrder)

    abstract def ___columnName(Column column, QContextType contextType)

    abstract QResultProxy ___partQuery(QueryPart q)

    abstract QResultProxy ___deleteQuery(QueryDelete q)

    abstract QResultProxy ___combinationQuery(QueryCombineType combineType, List<Object> queries)

    /**
     * Generates a select query using given query block instance.
     *
     * @param q input model of query.
     * @return generated select query.
     */
    abstract QResultProxy ___selectQuery(QuerySelect q)

    abstract QResultProxy ___insertQuery(QueryInsert q)

    abstract QResultProxy ___storedFunction(StoredFunction sp)

    abstract QResultProxy ___updateQuery(QueryUpdate q)

    def ___convertOperator(String op) {
        return op
    }

    def ___convertNumeric(Number number) {
        return String.valueOf(number)
    }

    abstract QDdl ___ddls()
}