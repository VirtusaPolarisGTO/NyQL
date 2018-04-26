package com.virtusa.gto.nyql.db;

import com.virtusa.gto.nyql.Column;
import com.virtusa.gto.nyql.Join;
import com.virtusa.gto.nyql.QContext;
import com.virtusa.gto.nyql.QuerySelect;
import com.virtusa.gto.nyql.Table;
import com.virtusa.gto.nyql.Where;
import com.virtusa.gto.nyql.model.JoinType;
import com.virtusa.gto.nyql.utils.QOperator;
import groovy.json.JsonSlurper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author IWEERARATHNA
 */
public class SqlMisc {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlMisc.class);

    @SuppressWarnings("unchecked")
    private static Collection<String> loadKeywords(InputStream inputStream) {
        Object parse = new JsonSlurper().parse(inputStream, StandardCharsets.UTF_8.name());
        return (List) parse;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadNameMappings(InputStream inputStream) {
        Object parse = new JsonSlurper().parse(inputStream, StandardCharsets.UTF_8.name());
        return (Map) parse;
    }

    public static Map<String, Object> loadNameMappings(String resourcePath, File keywordFileLocation) throws IOException {
        InputStream inputStream = null;
        Map<String, Object> map = new HashMap<>();
        try {
            if (keywordFileLocation != null && keywordFileLocation.exists()) {
                LOGGER.debug("Loading name mappings from " + keywordFileLocation);
                inputStream = new FileInputStream(keywordFileLocation);
            } else {
                LOGGER.debug("Loading name mappings from classpath " + resourcePath);
                inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            }

            if (inputStream != null) {
                map.putAll(loadNameMappings(inputStream));
            } else {
                LOGGER.warn("Could not load name mappings from classpath!");
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return map;
    }

    public static Set<String> loadKeywords(String resourcePath, File keywordFileLocation) throws IOException {
        InputStream inputStream = null;
        Set<String> klist = new HashSet<>();
        try {
            if (keywordFileLocation != null && keywordFileLocation.exists()) {
                LOGGER.debug("Loading keywords from " + keywordFileLocation);
                inputStream = new FileInputStream(keywordFileLocation);
            } else {
                LOGGER.debug("Loading keywords from classpath " + resourcePath);
                inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            }

            if (inputStream != null) {
                klist.addAll(loadKeywords(inputStream));
            } else {
                LOGGER.warn("Could not load reserved keyword list from classpath!");
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return klist;
    }

    public static int countJoin(Table table, JoinType joinType) {
        if (table == null) {
            return 0;
        }

        if (table instanceof Join) {
            Join join = (Join)table;
            if (join.getType() == joinType) {
                return 1 + countJoin(join.getTable1(), joinType);
            } else {
                return countJoin(join.getTable1(), joinType);
            }
        } else {
            return 0;
        }
    }

    public static void findJoins(Table table, JoinType joinType, List<Table> fulljoins) {
        if (table == null) {
            return;
        }

        if (table instanceof Join) {
            Join join = (Join)table;
            if (join.getType() == joinType) {
                fulljoins.add(join);
            }
            findJoins(join.getTable1(), joinType, fulljoins);
        }
    }

    public static void findFullJoins(Table table, List<Table> fulljoins) {
        if (table == null) {
            return;
        }

        if (table instanceof SqlFullJoin) {
            findFullJoins(((SqlFullJoin) table).getTable1(), fulljoins);
            fulljoins.add(table);
        }
    }

    public static QuerySelect cloneQuery(QuerySelect input) {
        QuerySelect q = new QuerySelect(input.get_ctx());
        q.set_distinct(input.get_distinct());
        q.set_intoColumns(input.get_intoColumns());
        q.set_intoTable(input.get_intoTable());
        q.set_joiningTable(cloneJoin(input.get_joiningTable()));
        q.setGroupBy(input.getGroupBy());
        q.setGroupByRollup(input.getGroupByRollup());
        q.setGroupHaving(input.getGroupHaving());
        q.setOffset(input.getOffset());
        q.setOrderBy(input.getOrderBy());
        q.setProjection(input.getProjection());
        q.set_limit(input.get_limit());
        q.setSourceTbl(input.getSourceTbl());
        q.setReturnType(input.getReturnType());
        q.setWhereObj(cloneWhere(input.getWhereObj(), input.get_ctx()));
        return q;
    }

    private static Where cloneWhere(Where other, QContext ctx) {
        if (other != null) {
            Where where = new Where(other.get_ctx());
            where.setClauses(new ArrayList<>(other.getClauses()));
            return where;
        } else {
            return new Where(ctx);
        }
    }

    private static Table cloneJoin(Table org) {
        if (org == null) {
            return null;
        }
        if (org instanceof Join) {
            Join join = (Join)org;
            Join cloned = join.getType() == JoinType.FULL_JOIN ? new SqlFullJoin() : new Join();
            cloned.setTable1(cloneJoin(join.getTable1()));
            cloned.setTable2(join.getTable2());
            cloned.setType(join.getType() == JoinType.FULL_JOIN ? JoinType.RIGHT_JOIN : join.getType());
            cloned.set_ctx(join.get_ctx());
            cloned.setOnConditions(join.getOnConditions());
            cloned.set__alias(join.get__alias());
            cloned.set__name(join.get__name());
            cloned.set__resultOf(join.get__resultOf());

            return cloned;
        } else {
            return org;
        }
    }

    public static void appendNullableConstraints(QuerySelect querySelect, int place) {
        if (place < 0) {
            return;
        }

        Table joiningTable = querySelect.get_joiningTable();
        List<Table> allFullJoins = new ArrayList<>();
        findFullJoins(joiningTable, allFullJoins);

        SqlFullJoin sqlFullJoin = (SqlFullJoin) allFullJoins.get(place);
        if (sqlFullJoin.___hasCondition()) {
            Where onConditions = sqlFullJoin.getOnConditions();
            if (onConditions != null) {
                List<Object> operands = new ArrayList<>();
                for (Object clause : onConditions.getClauses()) {
                    scanClauseForOtherTablesExcept(clause, sqlFullJoin.getTable2(), operands);
                }
                Where whereObj = querySelect.getWhereObj();
                List<Object> temp = new LinkedList<>();
                boolean hasClauses = whereObj.__hasClauses();
                for (int i = 0; i < operands.size(); i++) {
                    if (i > 0) {
                        temp.add(QOperator.AND);
                    }
                    temp.add(Where.__getIsNullClause(operands.get(i)));
                }
                if (hasClauses) {
                    temp.add(QOperator.AND);
                }

                whereObj.getClauses().addAll(0, temp);
                //querySelect.setWhereObj(whereObj);
            }
        }
    }

    private static void scanClauseForOtherTablesExcept(Object clause, Table exceptTbl, List<Object> operands) {
        if (clause == null) {
            return;
        }

        if (clause instanceof Where.QCondition) {
            Where.QCondition condition = (Where.QCondition) clause;
            scanClauseForOtherTablesExcept(condition.getLeftOp(), exceptTbl, operands);
            scanClauseForOtherTablesExcept(condition.getRightOp(), exceptTbl, operands);
        } else if (clause instanceof Where.QConditionGroup) {
            Where.QConditionGroup conditionGroup = (Where.QConditionGroup) clause;
            if (conditionGroup.getWhere() != null) {
                for (Object grpClause : conditionGroup.getWhere().getClauses()) {
                    scanClauseForOtherTablesExcept(grpClause, exceptTbl, operands);
                }
            }
        } else if (clause instanceof Column) {
            Column col = (Column) clause;
            if (!isColumnInTable(col, exceptTbl)) {
                operands.add(col);
            }
        }
    }

    private static boolean isColumnInTable(Column col, Table tbl) {
        Table owner = col.get_owner();
        if (tbl.__aliasDefined() && owner.__aliasDefined()) {
            return owner.get__alias().equals(tbl.get__alias());
        } else if (!tbl.__aliasDefined() && owner.__aliasDefined()) {
            return false;
        } else if (tbl.__aliasDefined() && !owner.__aliasDefined()) {
            return false;
        } else {
            return owner.get__name().equals(tbl.get__name());
        }
    }

    public static int flipNthFullJoin(Table org, int n, int curr) {
        if (n == 0 || org == null) {
            return curr;
        }

        if (org instanceof Join) {
            if (org instanceof SqlFullJoin) {
                curr++;
                if (n >= curr) {
                    ((SqlFullJoin)org).setType(JoinType.LEFT_JOIN);
                }
            }
            return flipNthFullJoin(((Join)org).getTable1(), n, curr);
        }
        return curr;
    }

    public static Table cloneTable(Table table, String newAlias) {
        Table t = new Table();
        t.set__name(table.get__name());
        if (newAlias != null) {
            t.set__alias(newAlias);
        }
        return t;
    }

}
