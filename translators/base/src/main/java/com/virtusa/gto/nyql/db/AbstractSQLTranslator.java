package com.virtusa.gto.nyql.db;

import com.virtusa.gto.nyql.*;
import com.virtusa.gto.nyql.exceptions.NyException;
import com.virtusa.gto.nyql.model.units.AParam;
import com.virtusa.gto.nyql.utils.QOperator;
import com.virtusa.gto.nyql.utils.QUtils;
import com.virtusa.gto.nyql.utils.QueryType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author IWEERARATHNA
 */
public abstract class AbstractSQLTranslator implements QTranslator {

    private static final String EMPTY = "";

    private final Collection<String> keywords;

    private static final String _AS_ = " AS ";

    protected AbstractSQLTranslator() {
        keywords = new HashSet<>();
    }

    protected AbstractSQLTranslator(Collection<String> theKeywords) {
        keywords = theKeywords;
    }

    protected String tableAlias(Table table, String qChar) {
        if (table.__aliasDefined()) {
            return (keywords.contains(table.get__alias().toUpperCase(Locale.getDefault()))
                    ? QUtils.quote(table.get__alias(), qChar)
                    : QUtils.quoteIfWS(table.get__alias(), qChar));
        } else {
            return EMPTY;
        }
    }

    protected String tableAliasAs(Table table, String qChar) {
        if (table.__aliasDefined()) {
            return _AS_ + (keywords.contains(table.get__alias().toUpperCase(Locale.getDefault()))
                            ? QUtils.quote(table.get__alias(), qChar)
                            : QUtils.quoteIfWS(table.get__alias(), qChar));
        } else {
            return EMPTY;
        }
    }

    protected String columnAlias(Column column, String qChar) {
        if (column.__aliasDefined()) {
            return (keywords.contains(column.get__alias().toUpperCase(Locale.getDefault()))
                    ? QUtils.quote(column.get__alias(), qChar)
                    : QUtils.quoteIfWS(column.get__alias(), qChar));
        } else {
            return EMPTY;
        }
    }

    protected String columnAliasAs(Column column, String qChar) {
        if (column.__aliasDefined()) {
            return _AS_ + (keywords.contains(column.get__alias().toUpperCase(Locale.getDefault()))
                        ? QUtils.quote(column.get__alias(), qChar)
                        : QUtils.quoteIfWS(column.get__alias(), qChar));
        } else {
            return EMPTY;
        }
    }

    @Override
    public QResultProxy ___partQuery(QueryPart q) throws NyException {
        List<AParam> paramList = new LinkedList<>();
        StringBuilder query = new StringBuilder();
        QueryType queryType = QueryType.PART;

        if (q.get_allProjections() != null) {
            query.append(___expandProjection(q.get_allProjections(), paramList, QContextType.SELECT));
            return createProxy(query.toString(), queryType, paramList, q.get_allProjections(), q);
        }

        if (q.getSourceTbl() != null) {
            query.append(___deriveSource(q.getSourceTbl(), paramList, QContextType.FROM));
            return createProxy(query.toString(), queryType, paramList, q.getSourceTbl(), q);
        }

        if (q.getWhereObj() != null) {
            query.append(___expandConditions(q.getWhereObj(), paramList, QContextType.CONDITIONAL));
            return createProxy(query.toString(), queryType, paramList, q.getWhereObj(), q);
        }

        if (q.get_assigns() != null) {
            query.append(___expandAssignments(q.get_assigns(), paramList, QContextType.UPDATE_SET));
            return createProxy(query.toString(), queryType, paramList, q.get_assigns(), q);
        }

        if (QUtils.notNullNorEmpty(q.get_intoColumns())) {
            query.append(___expandProjection(q.get_intoColumns(), paramList, QContextType.INSERT_PROJECTION));
            return createProxy(query.toString(), queryType, paramList, q.get_intoColumns(), q);
        }

        if (!QUtils.isNullOrEmpty(q.get_dataColumns())) {
            return createProxy("", queryType, paramList, q.get_dataColumns(), q);
        }
        throw new NyException("Parts are no longer supports to reuse other than WHERE and JOINING!");
    }

    private static QResultProxy createProxy(String query, QueryType queryType, List<AParam> params,
                                            Object raw, Query queryObject) {
        QResultProxy proxy = new QResultProxy();
        proxy.setQuery(query);
        proxy.setQueryType(queryType);
        proxy.setOrderedParameters(params);
        proxy.setRawObject(raw);
        proxy.setqObject(queryObject);
        return proxy;
    }

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


    protected void appendParamsFromTable(Table table, List<AParam> paramList) {
        if (table.__isResultOf()) {
            QResultProxy proxy = (QResultProxy) table.get__resultOf();
            if (proxy.getOrderedParameters() != null) {
                paramList.addAll(proxy.getOrderedParameters());
            }
        }
    }

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
                } else if (wrap instanceof AParam) {
                    paramList.add((AParam)wrap);
                }
            }
        }
    }

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

    protected String ___deriveSource(Table table, List<AParam> paramOrder, QContextType contextType) {
        if (table instanceof Join) {
            return ___tableJoinName((Join)table, contextType, paramOrder);
        } else {
            if (table.__isResultOf()) {
                QResultProxy proxy = (QResultProxy) table.get__resultOf();
                if (proxy.getOrderedParameters() != null) {
                    paramOrder.addAll(proxy.getOrderedParameters());
                }
            }
            return ___tableName(table, contextType);
        }
    }

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
