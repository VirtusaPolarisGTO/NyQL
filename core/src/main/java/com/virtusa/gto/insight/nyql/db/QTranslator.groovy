package com.virtusa.gto.insight.nyql.db

import com.virtusa.gto.insight.nyql.*
import com.virtusa.gto.insight.nyql.Where.QCondition
import com.virtusa.gto.insight.nyql.Where.QConditionGroup
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.params.AParam
import com.virtusa.gto.insight.nyql.model.params.ParamList
import com.virtusa.gto.insight.nyql.utils.QUtils
import com.virtusa.gto.insight.nyql.utils.QueryCombineType
import com.virtusa.gto.insight.nyql.utils.QueryType

import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * @author Isuru Weerarathna
 */
trait QTranslator extends QJoins {

    String NULL() { "NULL" }
    String COMPARATOR_NULL() { "IS" }

    def ___ifColumn(Case aCaseCol, List<AParam> paramOrder=null) {
        StringBuilder query = new StringBuilder("CASE")
        List<Case.CaseCondition> conditions = aCaseCol.allConditions
        for (Case.CaseCondition cc : conditions) {
            query.append(" WHEN ").append(___expandConditions(cc._theCondition, paramOrder, QContextType.CONDITIONAL))
            query.append(" THEN ").append(___resolve(cc._theResult, QContextType.SELECT))
        }

        if (aCaseCol.getElse() != null) {
            query.append(" ELSE ").append(___resolve(aCaseCol.getElse(), QContextType.SELECT))
        }
        query.append(" END")

        if (aCaseCol.__aliasDefined()) {
            query.append(" AS ").append(aCaseCol.__alias)
        }
        return query.toString()
    }

    def ___resolve(Object obj, QContextType contextType, List<AParam> paramOrder=null) {
        if (obj == null) {
            return NULL()
        }

        if (obj instanceof Join) {
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
            throw new Exception("Unsupported data object to convert! [" + obj + ", type: " + obj.class + "]")
        }
    }

    abstract def ___quoteString(String text)

    abstract def ___convertBool(Boolean value)

    abstract def ___tableName(Table table, QContextType contextType)

    abstract def ___tableJoinName(Join join, QContextType contextType, List<AParam> paramOrder)

    abstract def ___columnName(Column column, QContextType contextType)

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
            query.append(___expandAssignments(q._assigns, paramList, QContextType.UPDATE))
            return new QResultProxy(query: query.toString(), queryType: queryType,
                        orderedParameters: paramList, rawObject: q._assigns, qObject: q)
        }

        throw new NyException("Parts are no longer supports to reuse other than WHERE and JOINING!")
    }

    QResultProxy ___deleteQuery(QueryDelete q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()
        query.append("DELETE FROM ").append(___deriveSource(q.sourceTbl, paramList, QContextType.FROM)).append("\n")
        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append(" WHERE ").append(___expandConditions(q.whereObj, paramList, QContextType.CONDITIONAL)).append("\n")
        }
        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.DELETE);
    }

    QResultProxy ___combinationQuery(QueryCombineType combineType, List<Object> queries) {
        List<AParam> paramList = new LinkedList<>()
        Stream<Object> stream = queries.stream().map({ q ->
            if (q instanceof QResultProxy) {
                paramList.addAll(q.orderedParameters ?: [])
                return "(" + ___resolve(q, QContextType.UNKNOWN) + ")"
            } else {
                return ___resolve(q, QContextType.UNKNOWN, paramList)
            }
        });

        String qStr;
        if (combineType == QueryCombineType.UNION) {
            qStr = stream.collect(Collectors.joining("\nUNION ALL\n"))
        } else if (combineType == QueryCombineType.UNION_DISTINCT) {
            qStr = stream.collect(Collectors.joining("\nUNION\n"))
        } else {
            qStr = stream.collect(Collectors.joining("; "))
        }
        return new QResultProxy(query: qStr, orderedParameters: paramList, queryType: QueryType.SELECT, qObject: queries)
    }

    QResultProxy ___selectQuery(QuerySelect q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()
        QueryType queryType = QueryType.SELECT
        if (q._intoTable != null) {
            queryType = QueryType.INSERT
            query.append("INSERT INTO ").append(___tableName(q._intoTable, QContextType.INTO)).append(" ")
            if (QUtils.notNullNorEmpty(q._intoColumns)) {
                query.append(q._intoColumns.stream().map({
                        return ___columnName(it, QContextType.INTO)
                    }).collect(Collectors.joining(", ", "(", ")")))
            }
            query.append("\n")
        }

        query.append("SELECT ");
        if (q._distinct) {
            query.append("DISTINCT ")
        }
        query.append(___expandProjection(q.projection, paramList)).append("\n")
        query.append(" FROM ").append(___deriveSource(q._joiningTable ?: q.sourceTbl, paramList, QContextType.FROM)).append("\n")

        if (q.whereObj != null && q.whereObj.__hasClauses()) {
            query.append(" WHERE ").append(___expandConditions(q.whereObj, paramList, QContextType.CONDITIONAL)).append("\n")
        }

        if (QUtils.notNullNorEmpty(q.groupBy)) {
            query.append(" GROUP BY ").append(q.groupBy.stream()
                    .map({ Object t -> return ___columnName(t, QContextType.GROUP_BY) })
                    .collect(Collectors.joining(", ")))

            if (q.groupHaving != null) {
                query.append("\n").append(" HAVING ").append(___expandConditions(q.groupHaving, paramList, QContextType.GROUP_BY))
            }
            query.append("\n")
        }

        if (QUtils.notNullNorEmpty(q.orderBy)) {
            query.append(" ORDER BY ").append(q.orderBy.stream()
                    .map({ t -> return ___columnName(t, QContextType.ORDER_BY) })
                    .collect(Collectors.joining(", ")))
                    .append("\n")
        }

        if (q._limit != null) {
            if (q._limit instanceof Integer && ((Integer) q._limit) > 0) {
                query.append(" LIMIT ").append(String.valueOf(q._limit)).append("\n")
            } else if (q._limit instanceof AParam) {
                paramList.add((AParam) q._limit)
                query.append(" LIMIT ").append(___resolve(q._limit, QContextType.ORDER_BY)).append("\n")
            }
        }

        if (q.offset != null) {
            if (q.offset instanceof Integer && ((Integer) q.offset) >= 0) {
                query.append(" OFFSET ").append(String.valueOf(q.offset)).append("\n")
            } else if (q.offset instanceof AParam) {
                paramList.add((AParam) q.offset)
                query.append(" OFFSET ").append(___resolve(q.offset, QContextType.ORDER_BY)).append("\n")
            }
        }

        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: queryType, qObject: q)
    }

    QResultProxy ___insertQuery(QueryInsert q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()

        query.append("INSERT INTO ").append(___resolve(q._targetTable, QContextType.FROM, paramList)).append(" (")
        List<String> colList = new LinkedList<>()
        List<String> valList = new LinkedList<>()
        q._data.each { k, v ->
            colList.add(k)

            if (v instanceof AParam) {
                paramList.add((AParam)v)
            }
            valList.add(String.valueOf(___resolve(v, QContextType.CONDITIONAL)))
        }
        query.append(colList.stream().collect(Collectors.joining(", ")))
                .append(") VALUES (")
                .append(valList.stream().collect(Collectors.joining(", ")))
                .append(")")

        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.INSERT, qObject: q)
    }

    abstract QResultProxy ___storedFunction(StoredFunction sp)

    abstract QResultProxy ___updateQuery(QueryUpdate q)

    def ___convertOperator(String op) {
        return op
    }

    def ___convertNumeric(Number number) {
        return String.valueOf(number)
    }

    String ___expandProjection(List<Object> columns, List<AParam> paramList) {
        List<String> cols = new ArrayList<>()
        if (columns == null || columns.isEmpty()) {
            return "*"
        }

        List<Object> finalCols = new LinkedList<>()
        for (c in columns) {
            if (c instanceof QResultProxy) {
                if (c.queryType != QueryType.PART) {
                    throw new NyException("Only query parts allowed to import within sql projection!")
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
        return cols.stream().collect(Collectors.joining(", "))
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
            } else if (c instanceof QCondition) {
                builder.append(___expandCondition(c, paramOrder, contextType))
            } else if (c instanceof QConditionGroup) {
                builder.append("(").append(___expandConditionGroup(c, paramOrder, contextType)).append(")")
            }
        }

        return builder.toString()
    }

    String ___expandCondition(QCondition c, List<AParam> paramOrder, QContextType contextType) {
        if (c.leftOp instanceof AParam) {
            paramOrder?.add((AParam)c.leftOp)
        }
        ___scanForParameters(c.rightOp, paramOrder)
        boolean parenthesis = (c.rightOp instanceof QResultProxy)

        return ___resolve(c.leftOp, contextType) +
                (c.op.length() > 0 ? " " + c.op + " " : " ") +
                (!parenthesis ? ___resolve(c.rightOp, contextType) : "(" + ___resolve(c.rightOp, contextType) + ")")
    }

    String ___expandConditionGroup(QConditionGroup group, List<AParam> paramOrder, QContextType contextType) {
        String gCon = group.condConnector.isEmpty() ? "" : " " + group.condConnector + " ";
        return group.where.clauses.stream()
                .map({ c -> if (c instanceof QCondition) {
                            return ___expandCondition(c, paramOrder, contextType)
                        } else if (c instanceof QConditionGroup) {
                            return "(" + ___expandConditionGroup(c, paramOrder, contextType) + ")"
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

                derived.add(___resolve(c.leftOp, QContextType.CONDITIONAL, paramOrder) + " " + c.op + " " + ___resolve(c.rightOp, QContextType.CONDITIONAL, paramOrder))
             }
        }

        return derived.stream().collect(Collectors.joining(", "))
    }

    abstract QDdl ___ddls()
}