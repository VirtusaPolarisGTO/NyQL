package com.virtusa.gto.nyql.db

import com.virtusa.gto.nyql.*
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.JoinType
import com.virtusa.gto.nyql.model.QDbBootstrappable
import com.virtusa.gto.nyql.model.QScriptList
import com.virtusa.gto.nyql.model.QSession
import com.virtusa.gto.nyql.model.ValueTable
import com.virtusa.gto.nyql.model.units.*
import com.virtusa.gto.nyql.utils.QOperator
import com.virtusa.gto.nyql.utils.QUtils
import com.virtusa.gto.nyql.utils.QueryCombineType
import groovy.transform.CompileStatic
/**
 * @author Isuru Weerarathna
 */
trait QTranslator implements QDbBootstrappable {

    String NULL() { 'NULL' }

    @CompileStatic
    String ___resolve(Object obj, QContextType contextType, List<AParam> paramOrder=null) {
        if (obj == null) {
            return NULL()
        }

        if (obj instanceof QString) {
            return ___quoteString(obj.text) + (obj.__aliasDefined() && contextType == QContextType.SELECT ? ' AS ' + obj.__alias : '')
        } else if (obj instanceof QNumber) {
            return ___convertNumeric(obj.number) + (obj.__aliasDefined() && contextType == QContextType.SELECT ? ' AS ' + obj.__alias : '')
        } else if (obj instanceof QBoolean) {
            return ___convertBool(obj.value) + (obj.__aliasDefined() && contextType == QContextType.SELECT ? ' AS ' + obj.__alias : '')
        } else if (obj instanceof Join) {
            return ___tableJoinName(obj, contextType, paramOrder)
        } else if (obj instanceof Table) {
            return ___tableName(obj, contextType)
        } else if (obj instanceof Column) {
            return ___columnName(obj, contextType, paramOrder)
        } else if (obj instanceof Boolean) {
            return ___convertBool(obj)
        } else if (obj instanceof String || obj instanceof GString) {
            return String.valueOf(obj)
        } else if (obj instanceof Number) {
            return ___convertNumeric(obj)
        } else if (obj instanceof AParam) {
            if (paramOrder != null) {
                paramOrder.add((AParam)obj)
            }
            if (obj instanceof ParamList) {
                return QUtils.padParamList((String)obj.__name)
            }
            return '?' + (obj.__aliasDefined() && contextType == QContextType.SELECT ? ' AS ' + obj.__alias : '')
        } else if (obj instanceof QResultProxy) {
            return (obj.query ?: '').trim()
        } else if (obj instanceof List) {
            return QUtils.join((List)obj, { ___resolve(it, contextType, paramOrder) }, ', ', '(', ')')
            //return obj.stream().map { (String) }.collect(Collectors.joining(', ', '(', ')'))
        } else {
            throw new NyException('Unsupported data object to convert! [' + obj + ', type: ' + obj.class + ']')
        }
    }


    abstract String ___ifColumn(Case aCaseCol, List<AParam> paramOrder)

    /**
     * Transform the given text suitable inside a query. You may add proper
     * quoting here.
     *
     * @param text text to transform.
     * @return transformed text.
     */
    abstract String ___quoteString(String text)

    /**
     * Converts a boolean value to database specific representation in the query.
     *
     * @param value boolean value.
     * @return string representation of boolean value inside a query.
     */
    abstract String ___convertBool(Boolean value)

    /**
     * Converts a table name to string according to the given context.
     *
     * @param table table instance.
     * @param contextType context type.
     * @return string representation of the table.
     */
    abstract String ___tableName(Table table, QContextType contextType)

    /**
     * Converts a join chain to a string according to the given context.
     *
     * @param join join instance having a left and right table.
     * @param contextType context type.
     * @param paramOrder list of parameters.
     * @return string representation of the join.
     */
    abstract String ___tableJoinName(Join join, QContextType contextType, List<AParam> paramOrder)

    /**
     * Converts a column to string according to the given context.
     *
     * @param column column instance.
     * @param contextType context type.
     * @param paramList parameter list.
     * @return string representation of the column.
     */
    abstract String ___columnName(Column column, QContextType contextType, List<AParam> paramList)

    /**
     * Generate a query part which could be reusable among other queries.
     *
     * @param q input query part model.
     * @return generated query.
     */
    abstract QResultProxy ___partQuery(QueryPart q) throws NyException

    /**
     * Generate a delete query using given query model.
     *
     * @param q delete query model.
     * @return generated query.
     */
    abstract QResultProxy ___deleteQuery(QueryDelete q)

    /**
     * Generate a truncate query using given query model.
     *
     * @param q truncate query model.
     * @return generated query
     */
    abstract QResultProxy ___truncateQuery(QueryTruncate q)

    /**
     * Generate q query composed of several queries with combinators like
     * UNION, EXCEPT, etc.
     *
     * @param q query model.
     * @return generated query.
     */
    abstract QResultProxy ___combinationQuery(QueryCombineType combineType, List<Object> queries)

    /**
     * Generates a select query using given query block instance.
     *
     * @param q input model of query.
     * @return generated select query.
     */
    abstract QResultProxy ___selectQuery(QuerySelect q) throws NyException

    /**
     * Generate an insert query using given query model.
     *
     * @param q insert query model.
     * @return generated query.
     */
    abstract QResultProxy ___insertQuery(QueryInsert q)

    /**
     * Generate a query which can be executed as a stored function in database.
     *
     * @param q stored function model.
     * @return generated query.
     */
    abstract QResultProxy ___storedFunction(StoredFunction sp)

    /**
     * Generate an update query using given query model.
     *
     * @param q update query model.
     * @return generated query.
     */
    abstract QResultProxy ___updateQuery(QueryUpdate q)

    /**
     * Generates a db table from given set of values, so join clauses can use them.
     *
     * @param valueTable values instances.
     * @return created table instance.
     */
    abstract QResultProxy ___valueTable(ValueTable valueTable) throws NyException

    /**
     * Return resolved name for the given join type.
     *
     * @param joinType join type.
     * @return resolved name for join.
     */
    String ___resolveJoinType(JoinType joinType) {
        return joinType.getJoinName()
    }

    /**
     * Converts a given operator to appropriate db specific string.
     *
     * @param op operator.
     * @return string representation of operator.
     */
    String ___convertOperator(QOperator op) {
        return op.getOp()
    }

    /**
     * Converts a numeric value to database specific representation in the query.
     *
     * @param value numeric value.
     * @return string representation of numeric value inside a query.
     */
    String ___convertNumeric(Number number) {
        String.valueOf(number)
    }

    /**
     * Returns the DDL translator for this database.
     *
     * @return the DDL translator.
     */
    abstract QDdl ___ddls()

    @Override
    QScriptList getBootstrapScripts(QSession session) throws NyException {
        return null
    }
}