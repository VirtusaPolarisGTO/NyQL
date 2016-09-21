package com.virtusa.gto.insight.nyql.db

import com.virtusa.gto.insight.nyql.*
import com.virtusa.gto.insight.nyql.Where.QCondition
import com.virtusa.gto.insight.nyql.Where.QConditionGroup
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.utils.QUtils
import com.virtusa.gto.insight.nyql.utils.QueryType

import java.util.stream.Collectors

/**
 * @author Isuru Weerarathna
 */
trait QTranslator extends QJoins {

    String NULL = "NULL"
    String COMPARATOR_NULL = "IS"

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
            return "?" // ":" + obj.__name
        } else if (obj instanceof QResultProxy) {
            return obj.query
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

    QResultProxy ___selectQuery(QuerySelect q) {
        List<AParam> paramList = new LinkedList<>()
        StringBuilder query = new StringBuilder()
        QueryType queryType = QueryType.SELECT
        if (q._intoTable != null) {
            queryType = QueryType.INSERT
            query.append("INSERT INTO ").append(___deriveSource(q._intoTable, paramList, QContextType.FROM)).append(" \n")
        }

        query.append("SELECT ").append(___expandProjection(q.projection)).append("\n")
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

        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: queryType)
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

        return new QResultProxy(query: query.toString(), orderedParameters: paramList, queryType: QueryType.INSERT)
    }

    abstract QResultProxy ___storedFunction(StoredFunction sp)

    abstract QResultProxy ___updateQuery(QueryUpdate q)

    def ___convertOperator(String op) {
        return op
    }

    def ___convertNumeric(Number number) {
        return String.valueOf(number)
    }

    def ___expandProjection(List<Object> columns) {
        List<String> cols = new ArrayList<>()
        if (columns == null || columns.isEmpty()) {
            return "*"
        }

        for (c in columns) {
            if (c instanceof String) {
                cols.add(c)
            } else if (c instanceof Table) {
                String tbName = ___tableName((Table)c, QContextType.SELECT)
                cols.add("$tbName.*")
            } else if (c instanceof Column) {
                String cName = ___columnName(c, QContextType.SELECT)
                cols.add("$cName");
            }
        }
        return cols.stream().collect(Collectors.joining(", "))
    }

    def ___deriveSource(Table table, List<AParam> paramOrder, QContextType contextType) {
        if (table instanceof Join) {
            return ___tableJoinName(table, contextType, paramOrder)
        } else {
            return ___tableName(table, contextType)
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
        if (c.rightOp instanceof AParam) {
            paramOrder?.add((AParam)c.rightOp)
        }
        if (c.rightOp instanceof QResultProxy) {
            QResultProxy resultProxy = c.rightOp
            paramOrder?.addAll(resultProxy.orderedParameters ?: [])
        }

        return ___resolve(c.leftOp, contextType) + " " + c.op + " " + ___resolve(c.rightOp, contextType)
    }

    String ___expandConditionGroup(QConditionGroup group, List<AParam> paramOrder, QContextType contextType) {
        return group.where.clauses.stream()
                .map({ c -> if (c instanceof QCondition) {
                            return ___expandCondition(c, paramOrder, contextType)
                        } else if (c instanceof QConditionGroup) {
                            return "(" + ___expandConditionGroup(c, paramOrder, contextType) + ")"
                        } else {
                            return String.valueOf(c)
                        }
                }).collect(Collectors.joining(" " + group.condConnector + " "))
    }

    String ___expandAssignments(Assign assign, List<AParam> paramOrder, QContextType contextType=QContextType.UNKNOWN) {
        //StringBuilder builder = new StringBuilder()
        List<Object> clauses = assign.assignments
        List<String> derived = new ArrayList<>()
        for (c in clauses) {
            if (c instanceof String) {
                derived.add(c)
            } else if (c instanceof Assign.AnAssign) {
                if (c.leftOp instanceof AParam) {
                    paramOrder.add((AParam)c.leftOp)
                }
                if (c.rightOp instanceof AParam) {
                    paramOrder.add((AParam)c.rightOp)
                }
                if (c.rightOp instanceof QResultProxy) {
                    QResultProxy resultProxy = c.rightOp
                    paramOrder?.addAll(resultProxy.orderedParameters ?: [])
                }
                derived.add(___resolve(c.leftOp, QContextType.CONDITIONAL, paramOrder) + " " + c.op + " " + ___resolve(c.rightOp, QContextType.CONDITIONAL, paramOrder))
             }
        }

        return derived.stream().collect(Collectors.joining(", "))
    }

    abstract QDdl ___ddls()
}