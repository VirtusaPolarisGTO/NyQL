package com.virtusa.gto.nyql.db;

import com.virtusa.gto.nyql.*;
import com.virtusa.gto.nyql.exceptions.NyException;
import com.virtusa.gto.nyql.model.units.AParam;
import com.virtusa.gto.nyql.utils.QOperator;
import com.virtusa.gto.nyql.utils.QUtils;
import com.virtusa.gto.nyql.utils.QueryType;
import groovy.transform.CompileStatic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author IWEERARATHNA
 */
public abstract class AbstractSQLTranslator implements QTranslator {

    @CompileStatic
    protected String ___expandProjection(List<Object> columns, List<AParam> paramList, QContextType contextType) throws NyException {
        List<String> cols = new ArrayList<>();
        if (columns == null || columns.isEmpty()) {
            return "*";
        }

        List<Object> finalCols = new LinkedList<>();
        for (Object c : columns) {
            if (c instanceof QResultProxy) {
                if (((QResultProxy)c).getQueryType() != QueryType.PART) {
                    throw new NyException("Only query parts allowed to import within sql projection!");
                }
                List otherColumns = (List) ((QResultProxy)c).getRawObject();
                finalCols.addAll(otherColumns);
            } else if (c instanceof List) {
                finalCols.addAll((List)c);
            } else {
                finalCols.add(c);
            }
        }

        for (Object c : finalCols) {
            ___scanForParameters(c, paramList);

            if (c instanceof Table) {
                //appendParamsFromTable((Table)c, paramList)
                String tbName = ___tableName((Table)c, contextType);
                if (((Table)c).__isResultOf()) {
                    cols.add(tbName);
                } else {
                    cols.add(tbName + ".*");
                }
            } else if (c instanceof Column) {
                appendParamsFromColumn((Column)c, paramList);
                String cName = ___columnName((Column)c, contextType, paramList);
                cols.add(cName);
            } else if (c instanceof String) {
                cols.add((String)c);
            } else {
                cols.add(String.valueOf(___resolve(c, contextType, paramList)));
            }
        }
        return cols.stream().collect(Collectors.joining(", "));
    }

    @CompileStatic
    private static void appendParamsFromColumn(Column column, List<AParam> paramList) {
        if (column instanceof FunctionColumn) {
            if (((FunctionColumn) column).get_setOfCols()) {
                for (Object it : ((FunctionColumn) column).get_columns()) {
                    if (it instanceof QResultProxy && ((QResultProxy)it).getOrderedParameters() != null) {
                        paramList.addAll(((QResultProxy)it).getOrderedParameters());
                    }
                }
            } else {
                Object wrap = ((FunctionColumn) column).get_wrapper();
                if (wrap instanceof QResultProxy && ((QResultProxy)wrap).getOrderedParameters() != null) {
                    paramList.addAll(((QResultProxy)wrap).getOrderedParameters());
                }
            }
        }
    }

    @CompileStatic
    protected void ___scanForParameters(Object expression, List<AParam> paramOrder) {
        if (expression != null) {
            if (expression instanceof AParam) {
                addSafely(paramOrder, (AParam)expression);
            }

            if (expression instanceof QResultProxy) {
                QResultProxy resultProxy = (QResultProxy)expression;
                if (resultProxy.getOrderedParameters() != null) {
                    paramOrder.addAll(resultProxy.getOrderedParameters());
                }
            }
            if (expression instanceof Table && ((Table)expression).__isResultOf()) {
                QResultProxy resultProxy = (QResultProxy)(((Table)expression).get__resultOf());
                if (resultProxy.getOrderedParameters() != null) {
                    paramOrder.addAll(resultProxy.getOrderedParameters());
                }
            }
            if (expression instanceof FunctionColumn) {
                ___expandColumn((FunctionColumn) expression, paramOrder);
            }
            if (expression instanceof List) {
                for (Object it : (List)expression) {
                    ___scanForParameters(it, paramOrder);
                }
            }
        }
    }

    @CompileStatic
    protected void ___expandColumn(Column column, List<AParam> paramList) {
        if (column instanceof FunctionColumn && ((FunctionColumn) column).get_columns() != null) {
            for (Object it : ((FunctionColumn) column).get_columns()) {
                if (it instanceof FunctionColumn) {
                    ___expandColumn((FunctionColumn)it, paramList);
                } else if (it instanceof AParam) {
                    paramList.add((AParam)it);
                }
            }
        }
    }

    @CompileStatic
    protected String ___expandConditions(Where where, List<AParam> paramOrder, QContextType contextType) {
        StringBuilder builder = new StringBuilder();
        List<Object> clauses = where.getClauses();
        for (Object c : clauses) {
            if (c instanceof String) {
                builder.append(c);
            } else if (c instanceof QOperator) {
                builder.append(' ').append(___convertOperator((QOperator)c)).append(' ');
            } else if (c instanceof Where.QCondition) {
                builder.append(___expandCondition((Where.QCondition)c, paramOrder, contextType));
            } else if (c instanceof Where.QConditionGroup) {
                builder.append(QUtils.parenthesis(
                        ___expandConditionGroup((Where.QConditionGroup)c, paramOrder, contextType)));
            }
        }

        return builder.toString();
    }

    @CompileStatic
    protected String ___expandCondition(Where.QCondition c, List<AParam> paramOrder, QContextType contextType) {
        if (c.getLeftOp() instanceof AParam) {
            paramOrder.add((AParam)c.getLeftOp());
        }
        ___scanForParameters(c.getRightOp(), paramOrder);
        boolean parenthesis = (c.getRightOp() instanceof QResultProxy);

        if (c instanceof Where.QUnaryCondition) {
            return ___convertOperator(c.getOp()) + ' ' +
                    (parenthesis ?
                            QUtils.parenthesis(___resolve(((Where.QUnaryCondition) c).chooseOp(), contextType, paramOrder))
                            : ___resolve(((Where.QUnaryCondition) c).chooseOp(), contextType, paramOrder));
        } else {
            return ___resolve(c.getLeftOp(), contextType) +
                    (c.getOp() != QOperator.UNKNOWN ?
                            ' ' + ___convertOperator(c.getOp()) + ' ' : ' ') + (!parenthesis ? ___resolve(c.getRightOp(), contextType)
                            : QUtils.parenthesis(___resolve(c.getRightOp(), contextType)));
        }
    }

    @CompileStatic
    protected String ___expandConditionGroup(Where.QConditionGroup group, List<AParam> paramOrder, QContextType contextType) {
        String gCon = group.getCondConnector() == null ? "" : " " + ___convertOperator(group.getCondConnector()) + " ";
        List<String> list = new LinkedList<>();

        for (Object clause : group.getWhere().getClauses()) {
            if (clause instanceof Where.QCondition) {
                list.add(___expandCondition((Where.QCondition)clause, paramOrder, contextType));
            } else if (clause instanceof Where.QConditionGroup) {
                list.add(QUtils.parenthesis(
                        ___expandConditionGroup((Where.QConditionGroup)clause, paramOrder, contextType)));
            } else {
                list.add(___resolve(clause, contextType, paramOrder));
            }
        }
        return list.stream().collect(Collectors.joining(gCon));
    }

    @CompileStatic
    protected String ___expandAssignments(Assign assign, List<AParam> paramOrder, QContextType contextType) {
        List<Object> clauses = assign.getAssignments();
        List<String> derived = new ArrayList<>();
        for (Object c : clauses) {
            if (c instanceof String) {
                derived.add((String)c);
            } else if (c instanceof Assign.AnAssign) {
                Assign.AnAssign anAssign = (Assign.AnAssign)c;
                if (anAssign.getLeftOp() instanceof AParam) {
                    paramOrder.add((AParam)anAssign.getLeftOp());
                }
                ___scanForParameters(anAssign.getRightOp(), paramOrder);

                String val = ___resolve(anAssign.getLeftOp(), contextType, paramOrder) +
                        ' ' + ___convertOperator(anAssign.getOp()) + ' ' +
                        ___resolve(anAssign.getRightOp(), contextType, paramOrder);
                derived.add(val);
            }
        }

        return derived.stream().collect(Collectors.joining(", "));
    }

    private <T> List<T> addSafely(List<T> list, T item) {
        if (list != null) {
            list.add(item);
        }
        return list;
    }

}
