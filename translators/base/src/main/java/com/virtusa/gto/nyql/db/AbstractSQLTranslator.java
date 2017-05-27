package com.virtusa.gto.nyql.db;

import com.virtusa.gto.nyql.Assign;
import com.virtusa.gto.nyql.Column;
import com.virtusa.gto.nyql.FunctionColumn;
import com.virtusa.gto.nyql.Join;
import com.virtusa.gto.nyql.QContextType;
import com.virtusa.gto.nyql.QResultProxy;
import com.virtusa.gto.nyql.Query;
import com.virtusa.gto.nyql.QueryInsert;
import com.virtusa.gto.nyql.QueryPart;
import com.virtusa.gto.nyql.QuerySelect;
import com.virtusa.gto.nyql.QueryTruncate;
import com.virtusa.gto.nyql.Table;
import com.virtusa.gto.nyql.Where;
import com.virtusa.gto.nyql.exceptions.NyException;
import com.virtusa.gto.nyql.model.units.AParam;
import com.virtusa.gto.nyql.utils.QOperator;
import com.virtusa.gto.nyql.utils.QUtils;
import com.virtusa.gto.nyql.utils.QueryType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author IWEERARATHNA
 */
public abstract class AbstractSQLTranslator implements QTranslator {

    private static final String EMPTY = "";

    private final TranslatorOptions translatorOptions;
    private final Collection<String> keywords;

    private static final String NL = "\n";
    private static final String _AS_ = " AS ";
    private static final String COMMA = ", ";

    protected AbstractSQLTranslator() {
        translatorOptions = TranslatorOptions.empty();
        keywords = translatorOptions.getKeywords();
    }

    protected AbstractSQLTranslator(TranslatorOptions theOptions) {
        if (theOptions != null) {
            translatorOptions = theOptions;
        } else {
            translatorOptions = TranslatorOptions.empty();
        }
        keywords = translatorOptions.getKeywords();
    }

    protected TranslatorOptions getTranslatorOptions() {
        return translatorOptions;
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

    /**
     * Returns correct table name for the given table name, if there is a name mapping for it.
     *
     * Usually this method will be used by Oracle implementation.
     *
     * @param tblName table name.
     * @return mapping table name.
     */
    protected final String tableName(String tblName) {
        return getTranslatorOptions().tableMapName(tblName);
    }

    /**
     * Returns correct column name of the table, if there is a name mapping for it.
     *
     * @param tblName table name.
     * @param colName column name.
     * @return mapping column name.
     */
    protected final String columnName(String tblName, String colName) {
        return getTranslatorOptions().columnMapName(tblName, colName);
    }

    protected String generateTableJoinName(final Join join, final String joinType, final QContextType contextType, List<AParam> paramOrder) {
        StringBuilder qstr = new StringBuilder();

        if (join.getTable1().__isResultOf()) {
            QResultProxy proxy = (QResultProxy) join.getTable1().get__resultOf();
            addAllSafely(paramOrder, proxy.getOrderedParameters());
        }
        qstr.append(___resolve(join.getTable1(), contextType, paramOrder));
        qstr.append(" ").append(joinType).append(" ");

        if (join.getTable2().__isResultOf()) {
            QResultProxy proxy = (QResultProxy) join.getTable2().get__resultOf();
            addAllSafely(paramOrder, proxy.getOrderedParameters());
        }
        qstr.append(___resolve(join.getTable2(), contextType, paramOrder));

        if (join.___hasCondition()) {
            qstr.append(" ON ").append(___expandConditions(join.getOnConditions(), paramOrder, QUtils.findDeleteContext(contextType)));
        }
        return qstr.toString();
    }

    protected QResultProxy generateInsertQuery(QueryInsert q, String quoteChar) throws NyException {
        if (QUtils.isNullOrEmpty(q.get_data()) && q.get_assigns() == null) {
            return ___selectQuery(q);
        }

        List<AParam> paramList = new LinkedList<>();
        StringBuilder query = new StringBuilder();

        query.append("INSERT INTO ").append(___resolve(q.getSourceTbl(), QContextType.INTO, paramList)).append(" (");
        List<String> colList = new LinkedList<>();
        List<String> valList = new LinkedList<>();

        if (q.get_data() != null) {
            for (Map.Entry<String, Object> entry : q.get_data().entrySet()) {
                colList.add(QUtils.quote(entry.getKey(), quoteChar));

                ___scanForParameters(entry.getValue(), paramList);
                valList.add(String.valueOf(___resolve(entry.getValue(), QContextType.INSERT_DATA, paramList)));
            }
        }

        if (q.get_assigns() != null && q.get_assigns().__hasAssignments()) {
            for (Object object : q.get_assigns().getAssignments()) {
                if (object instanceof Assign.AnAssign) {
                    Assign.AnAssign anAssign = (Assign.AnAssign)object;

                    if (anAssign.getLeftOp() instanceof Column) {
                        colList.add(String.valueOf(___resolve(anAssign.getLeftOp(), QContextType.INSERT_PROJECTION, paramList)));
                    } else {
                        colList.add(QUtils.quote(anAssign.toString(), quoteChar));
                    }
                    ___scanForParameters(anAssign.getRightOp(), paramList);
                    valList.add(String.valueOf(___resolve(anAssign.getRightOp(), QContextType.INSERT_DATA, paramList)));
                }
            }
        }

        query.append(colList.stream().collect(Collectors.joining(COMMA)))
                .append(") VALUES (")
                .append(valList.stream().collect(Collectors.joining(COMMA)))
                .append(")");

        QResultProxy resultProxy = createProxy(query.toString(), QueryType.INSERT, paramList, null, null);
        resultProxy.setReturnType(q.getReturnType());
        return resultProxy;
    }

    @Override
    public QResultProxy ___selectQuery(QuerySelect q) throws NyException {
        final List<AParam> paramList = new LinkedList<>();
        StringBuilder query = new StringBuilder();
        QueryType queryType = QueryType.SELECT;
        if (q.get_intoTable() != null) {
            queryType = QueryType.INSERT;
            query.append("INSERT INTO ").append(___tableName(q.get_intoTable(), QContextType.INTO)).append(' ');
            if (QUtils.notNullNorEmpty(q.get_intoColumns())) {
                query.append(QUtils.parenthesis(___expandProjection(q.get_intoColumns(), paramList, QContextType.INSERT_PROJECTION)))
                        .append(" ");
            }
            query.append(NL);
        }

        query.append("SELECT ");
        if (q.is_distinct()) {
            query.append("DISTINCT ");
        }
        query.append(___expandProjection(q.getProjection(), paramList, QContextType.SELECT)).append(NL);
        // target is optional
        if (q.get_joiningTable() != null) {
            query.append(" FROM ").append(___deriveSource(q.get_joiningTable(), paramList, QContextType.FROM)).append(NL);
        } else if (q.getSourceTbl() != null) {
            query.append(" FROM ").append(___deriveSource(q.getSourceTbl(), paramList, QContextType.FROM)).append(NL);
        }

        if (q.getWhereObj() != null && q.getWhereObj().__hasClauses()) {
            query.append(" WHERE ").append(___expandConditions(q.getWhereObj(), paramList, QContextType.CONDITIONAL)).append(NL);
        }

        if (QUtils.notNullNorEmpty(q.getGroupBy())) {
            ___selectQueryGroupByClause(q, query, paramList);
        }

        if (q.getGroupHaving() != null) {
            query.append(NL).append(" HAVING ").append(___expandConditions(q.getGroupHaving(), paramList, QContextType.HAVING));
            query.append(NL);
        }

        if (QUtils.notNullNorEmpty(q.getOrderBy())) {
            String oClauses = QUtils.join(q.getOrderBy(), it -> ___resolve(it, QContextType.ORDER_BY, paramList), COMMA, "", "");
            query.append(" ORDER BY ").append(oClauses).append(NL);
        }

        if (q.get_limit() != null) {
            if (q.get_limit() instanceof Integer && ((Integer) q.get_limit()) > 0) {
                query.append(" LIMIT ").append(String.valueOf(q.get_limit())).append(NL);
            } else if (q.get_limit() instanceof AParam) {
                paramList.add((AParam) q.get_limit());
                query.append(" LIMIT ").append(___resolve(q.get_limit(), QContextType.ORDER_BY)).append(NL);
            }
        }

        if (q.getOffset() != null) {
            if (q.getOffset() instanceof Integer && ((Integer) q.getOffset()) >= 0) {
                query.append(" OFFSET ").append(String.valueOf(q.getOffset())).append(NL);
            } else if (q.getOffset() instanceof AParam) {
                paramList.add((AParam) q.getOffset());
                query.append(" OFFSET ").append(___resolve(q.getOffset(), QContextType.ORDER_BY)).append(NL);
            }
        }

        return createProxy(query.toString(), queryType, paramList, null, null);
    }

    /**
     * Generated group by clause as it is different with rollup introduction.
     *
     * @param q input select query model.
     * @param query query string to generate.
     * @param paramList parameter list.
     * @throws NyException any exception thrown while generating.
     */
    protected void ___selectQueryGroupByClause(QuerySelect q, StringBuilder query, List<AParam> paramList) throws NyException {
        String gClauses = QUtils.join(q.getGroupBy(), it -> ___resolve(it, QContextType.GROUP_BY, paramList), COMMA, "", "");
        query.append(" GROUP BY ").append(gClauses);

        if (q.getGroupByRollup()) {
            // rollup enabled
            query.append(" WITH ROLLUP");
        }
    }

    @Override
    public QResultProxy ___truncateQuery(QueryTruncate q) {
        String query = "TRUNCATE TABLE " + ___tableName(q.getSourceTbl(), QContextType.TRUNCATE);
        return createProxy(query, QueryType.TRUNCATE, new ArrayList<>(), null, null);
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
        throw new NyException("Unknown or incomplete re-usable query clause!");
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
            } else if (expression instanceof QResultProxy) {
                QResultProxy resultProxy = (QResultProxy)expression;
                if (resultProxy.getOrderedParameters() != null) {
                    paramOrder.addAll(resultProxy.getOrderedParameters());
                }
            } else if (expression instanceof Table && ((Table)expression).__isResultOf()) {
                QResultProxy resultProxy = (QResultProxy)(((Table)expression).get__resultOf());
                if (resultProxy.getOrderedParameters() != null) {
                    paramOrder.addAll(resultProxy.getOrderedParameters());
                }
            } else if (expression instanceof FunctionColumn) {
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
            return ___resolveOperand(c.getLeftOp(), paramOrder, contextType) +
                    (c.getOp() != QOperator.UNKNOWN ? ' ' + ___convertOperator(c.getOp()) + ' ' : ' ') +
                    (!parenthesis ? ___resolveOperand(c.getRightOp(), paramOrder, contextType)
                            : QUtils.parenthesis(___resolveOperand(c.getRightOp(), paramOrder, contextType)));
        }
    }

    private String ___resolveOperand(Object operand, List<AParam> paramOrder, QContextType contextType) {
        if (operand instanceof Where.QCondition) {
            return ___expandCondition((Where.QCondition)operand, paramOrder, contextType);
        } else {
            return ___resolve(operand, contextType, paramOrder);
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

    private <T> List<T> addAllSafely(List<T> list, Collection<T> items) {
        if (list != null) {
            list.addAll(items);
        }
        return list;
    }
}
