package com.virtusa.gto.nyql.db;

import com.virtusa.gto.nyql.Join;
import com.virtusa.gto.nyql.QuerySelect;
import com.virtusa.gto.nyql.Table;
import com.virtusa.gto.nyql.model.JoinType;
import groovy.json.JsonSlurper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
        q.setWhereObj(input.getWhereObj());
        return q;
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

    public static Table flipNthFullJoin(Table org, int n, int curr) {
        if (n == 0 || org == null) {
            return org;
        }

        if (org instanceof Join) {
            if (org instanceof SqlFullJoin) {
                curr++;
                if (n >= curr) {
                    ((SqlFullJoin)org).setType(JoinType.LEFT_JOIN);
                }
            }
            flipNthFullJoin(((Join)org).getTable1(), n, curr);
        }
        return org;
    }


}
