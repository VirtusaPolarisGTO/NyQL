package com.virtusa.gto.insight.nyql.db.mssql

import com.virtusa.gto.insight.nyql.Assign
import com.virtusa.gto.insight.nyql.Case
import com.virtusa.gto.insight.nyql.Column
import com.virtusa.gto.insight.nyql.FunctionColumn
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
import com.virtusa.gto.insight.nyql.Where
import com.virtusa.gto.insight.nyql.db.QDdl
import com.virtusa.gto.insight.nyql.db.QTranslator
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.blocks.AParam
import com.virtusa.gto.insight.nyql.utils.QUtils
import com.virtusa.gto.insight.nyql.utils.QueryCombineType
import com.virtusa.gto.insight.nyql.utils.QueryType

import java.util.stream.Collectors

/**
 * @author IWEERARATHNA
 */
class MSSql implements QTranslator, MSSqlFunctions {

    private static final MSSqlDDL DDL = new MSSqlDDL()

    static final String DOUBLE_QUOTE = "\""
    static final String STR_QUOTE = "'"

    @Override
    def ___ifColumn(Case aCaseCol, List<AParam> paramOrder) {
        if (aCaseCol.caseType == Case.CaseType.IFNULL) {
            StringBuilder query = new StringBuilder("ISNULL(")
            def whenCondition = aCaseCol.allConditions.get(0)
            Where.QCondition qCondition = (Where.QCondition) whenCondition._theCondition.clauses.get(0)
            query.append(___resolve(qCondition.leftOp, QContextType.SELECT, paramOrder))
            query.append(", ")
            query.append(___resolve(whenCondition._theResult, QContextType.SELECT, paramOrder))
            query.append(")")

            if (aCaseCol.__aliasDefined()) {
                query.append(" AS ").append(aCaseCol.__alias)
            }
            return query.toString()

        } else {
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
    }

    @Override
    def ___quoteString(String text) {
        return QUtils.quote(text, STR_QUOTE)
    }

    @Override
    def ___convertBool(Boolean value) {
        return (value ? "True" : "False")
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
        return DDL
    }


    private String ___expandProjection(List<Object> columns, List<AParam> paramList) {
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

    private def ___deriveSource(Table table, List<AParam> paramOrder, QContextType contextType) {
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

    private void ___expandColumn(Column column, List<AParam> paramList) {
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

    private void ___scanForParameters(def expression, List<AParam> paramOrder) {
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

    private String ___expandConditions(Where where, List<AParam> paramOrder, QContextType contextType=QContextType.UNKNOWN) {
        StringBuilder builder = new StringBuilder()
        List<Object> clauses = where.clauses
        for (c in clauses) {
            if (c instanceof String) {
                builder.append(c)
            } else if (c instanceof Where.QCondition) {
                builder.append(___expandCondition(c, paramOrder, contextType))
            } else if (c instanceof Where.QConditionGroup) {
                builder.append("(").append(___expandConditionGroup(c, paramOrder, contextType)).append(")")
            }
        }

        return builder.toString()
    }

    private String ___expandCondition(Where.QCondition c, List<AParam> paramOrder, QContextType contextType) {
        if (c.leftOp instanceof AParam) {
            paramOrder?.add((AParam)c.leftOp)
        }
        ___scanForParameters(c.rightOp, paramOrder)
        boolean parenthesis = (c.rightOp instanceof QResultProxy)

        return ___resolve(c.leftOp, contextType) +
                (c.op.length() > 0 ? " " + c.op + " " : " ") +
                (!parenthesis ? ___resolve(c.rightOp, contextType) : "(" + ___resolve(c.rightOp, contextType) + ")")
    }

    private String ___expandConditionGroup(Where.QConditionGroup group, List<AParam> paramOrder, QContextType contextType) {
        String gCon = group.condConnector.isEmpty() ? "" : " " + group.condConnector + " ";
        return group.where.clauses.stream()
                .map({ c -> if (c instanceof Where.QCondition) {
            return ___expandCondition(c, paramOrder, contextType)
        } else if (c instanceof Where.QConditionGroup) {
            return "(" + ___expandConditionGroup(c, paramOrder, contextType) + ")"
        } else {
            return String.valueOf(c)
        }
        }).collect(Collectors.joining(gCon))
    }

    private String ___expandAssignments(Assign assign, List<AParam> paramOrder, QContextType contextType=QContextType.UNKNOWN) {
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
}
