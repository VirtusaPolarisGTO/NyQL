package com.virtusa.gto.insight.nyql.utils

import com.virtusa.gto.insight.nyql.Join
import com.virtusa.gto.insight.nyql.QContext
import com.virtusa.gto.insight.nyql.QContextType
import com.virtusa.gto.insight.nyql.Table
import com.virtusa.gto.insight.nyql.exceptions.NySyntaxException
import org.apache.commons.lang3.StringUtils

/**
 * @author Isuru Weerarathna
 */
class QUtils {

    static boolean notNullNorEmpty(Collection<?> col) {
        return col != null && !col.isEmpty()
    }

    static String quote(String text, String c="`") {
        return "$c$text$c"
    }

    static boolean hasWS(String text) {
        return StringUtils.containsWhitespace(text)
    }

    static String classNameFromScriptId(String scriptId) {
        return StringUtils.substringAfterLast(scriptId, "/");
    }

    static String quoteIfWS(String text, String c="`") {
        if (hasWS(text)) {
            return quote(text, c)
        } else {
            return text
        }
    }

    static Table mergeJoinClauses(QContext ctx, Table table1, Table table2, String type) {
        Table rmost = findRightMostTable(table1)
        Table lmost = findLeftMostTable(table2)
        if (rmost.__name == lmost.__name && rmost.__alias == lmost.__alias) {
            if (table1 instanceof Join) {
                if (table2 instanceof Join) {
                    return new Join(table1: table1, table2: table2.table2, _ctx: ctx, type: type)
                } else {
                    return table1
                }
            } else {
                if (table2 instanceof Join) {
                    return table2
                } else {
                    throw new NySyntaxException("Merging same table!")
                }
            }
        }
        return new Join(table1: table1, table2: table2, _ctx: ctx, type: type)
    }

    static Table findLeftMostTable(Table table) {
        if (table instanceof Join) {
            Join j = (Join)table
            return findLeftMostTable(j.table1)
        } else {
            return table
        }
    }

    static Table findRightMostTable(Table table) {
        if (table instanceof Join) {
            Join j = (Join)table
            return findLeftMostTable(j.table2)
        } else {
            return table
        }
    }

    static Table replaceLeftMostTable(Table search, Table withWhat) {
        if (search instanceof Join) {
            Join j = (Join)search
            Table t2 = findLeftMostTable(j.table1)
            if (t2 == j.table1) {
                j.table1 = withWhat
                return search
            }
            return t2
        } else {
            return search
        }
    }

    static def findAlTables(Table table, List<Table> tableList) {
        if (table instanceof Join) {
            Join j = (Join)table
            findAlTables(j.table1, tableList)
            findAlTables(j.table2, tableList)
        } else {
            tableList.add(table)
        }
    }

    static def filterAllJoinConditions(Table table, List<String> clauses, String joinStr) {
        if (table instanceof Join) {
            Join j = (Join)table

            if (j.___hasCondition()) {
                if (!clauses.isEmpty()) {
                    clauses.add(joinStr)
                }
                clauses.addAll(j.onConditions.clauses)
            }

            filterAllJoinConditions(j.table1, clauses, joinStr)
            filterAllJoinConditions(j.table2, clauses, joinStr)
        }
    }

    static void expandToList(List list, Object... items) {
        if (items == null) {
            return
        }
        for (def item : items) {
            if (item instanceof List) {
                item.each { expandToList(list, it) }
            } else {
                list.add(item)
            }
        }
    }

    private String __wrapF(def c, String funcName) {
        if (c instanceof String)
            return funcName + "($c)"
        else
            return funcName + "(" + ___columnName(c, QContextType.INSIDE_FUNCTION) + ")"
    }

}
