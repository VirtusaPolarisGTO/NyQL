package com.virtusa.gto.nyql.utils

import com.virtusa.gto.nyql.Join
import com.virtusa.gto.nyql.QContext
import com.virtusa.gto.nyql.Table
import com.virtusa.gto.nyql.TableProxy
import com.virtusa.gto.nyql.Where
import com.virtusa.gto.nyql.exceptions.NySyntaxException
import com.virtusa.gto.nyql.model.units.AParam
import com.virtusa.gto.nyql.model.units.NamedParam
import groovy.transform.CompileStatic

import java.util.function.Function

/**
 * @author Isuru Weerarathna
 */
@CompileStatic
final class QUtils {

    private static final String NL = '\n'
    private static final String P_PAD = '::'
    private static final String OP = '('
    private static final String CP = ')'

    static String join(List<Object> items, Function<Object, String> converter,
                                      String sep, String prefix, String suffix) {
        StringJoiner joiner = new StringJoiner(sep, prefix, suffix)
        for (Object val : items) {
            joiner.add(converter.apply(val))
        }
        joiner.toString()
    }

    static String generateErrStr(String mainError, String... helpLines) {
        StringBuilder builder = new StringBuilder().append(mainError)
        if (helpLines != null) {
            builder.append(NL)
            for (String line : helpLines) {
                builder.append('\t> ').append(line).append(NL)
            }
        }
        builder.toString()
    }

    /**
     * Enclose in parenthesis given text.
     *
     * @param text input text to enclose.
     * @return text enclosed inside parenthesis.
     */
    static String parenthesis(String text) {
        OP + text + CP
    }

    /**
     * Returns true if collection is not null nor empty.
     *
     * @param col collection to check.
     * @return true if it is not null nor empty.
     */
    static boolean notNullNorEmpty(Collection<?> col) {
        col != null && !col.isEmpty()
    }

    /**
     * Returns true if given map is null or empty.
     *
     * @param col collection to check.
     * @return true if it is null or empty.
     */
    static boolean isNullOrEmpty(Map<?, ?> map) {
        map == null || map.isEmpty()
    }

    /**
     * Quote the given text by padding given character.
     *
     * @param text text to be quoted.
     * @param c character to pad the string.
     * @return quoted string.
     */
    static String quote(String text, String c='`') {
        c + text + c
    }

    /**
     * Returns true if given text has any whitespace character.
     *
     * @param text text to check for whitespaces.
     * @return true if whitespaces are there.
     */
    static boolean hasWS(String text) {
        hasWhitespace(text)
    }

    /**
     * Returns true if given text has whitespaces.
     *
     * @param text string to check.
     * @return true if it has whitespaces.
     */
    private static boolean hasWhitespace(String text) {
        if (text == null || text.isEmpty()) {
            return false
        }
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return true
            }
        }
        false
    }

    /**
     * Quote the given string if it contains any whitespace.
     *
     * @param text text to quote.
     * @param c character to pad.
     * @return quoted string if has whitespaces. Otherwise same string.
     */
    static String quoteIfWS(String text, String c='`') {
        if (hasWS(text)) {
            quote(text, c)
        } else {
            text
        }
    }

    /**
     * Merges two tables/join chains together by carefully merging middle two tables if they are same.
     *
     * @param ctx context instance.
     * @param tableLeft left join chain.
     * @param tableRight right join chain.
     * @param type type of the join if it needed to be merged.
     * @return merged table reference.
     */
    static Table mergeJoinClauses(QContext ctx, Table tableLeft, Table tableRight, String type) {
        Table table1 = tableLeft
        Table table2 = tableRight
        Table rmost = findRightMostTable(table1)
        Table lmost = findLeftMostTable(table2)
        Where joinCond = null
        if (rmost.__name == lmost.__name && rmost.__alias == lmost.__alias) {
            if (table1 instanceof Join) {
                if (table2 instanceof Join) {
                    return new Join(table1: table1, table2: ((Join)table2).table2, _ctx: ctx, type: type)
                } else {
                    return table1
                }
            } else {
                if (table2 instanceof Join) {
                    return table2
                } else {
                    throw new NySyntaxException('You are trying to join same tables consecutively! At least table alias must be different!')
                }
            }
        }

        if (rmost instanceof TableProxy) {
            if (table1 instanceof Join) {
                joinCond = ((Join)table1).onConditions
                table1 = ((Join)table1).table1
            }
        }
        if (lmost instanceof TableProxy) {
            if (table2 instanceof Join) {
                replaceLeftMostTableWith((Join)table2, table1)
                return table2
            }
        }

        new Join(table1: table1, table2: table2, _ctx: ctx, type: type, onConditions: joinCond)
    }

    private static void replaceLeftMostTableWith(Join join, Table tableToReplace) {
        if (join.table1 instanceof Join) {
            replaceLeftMostTableWith((Join)join.table1, tableToReplace)
        } else {
            if (join.table1 instanceof TableProxy) {
                join.table1 = tableToReplace
            }
        }
    }

    /**
     * Find left most table of the join chain.
     *
     * @param table input join chain.
     * @return the left most table of chain.
     */
    static Table findLeftMostTable(Table table) {
        if (table instanceof Join) {
            Join j = (Join)table
            findLeftMostTable(j.table1)
        } else {
            table
        }
    }

    /**
     * Find right most table of the join chain.
     *
     * @param table input join chain.
     * @return the right most table of chain.
     */
    static Table findRightMostTable(Table table) {
        if (table instanceof Join) {
            Join j = (Join)table
            findLeftMostTable(j.table2)
        } else {
            table
        }
    }

    /**
     * Find all specified tables in the given join chain.
     *
     * @param table input join chain.
     * @param tableList list to add found tables.
     */
    static void findAlTables(Table table, List<Table> tableList) {
        if (table instanceof Join) {
            Join j = (Join)table
            findAlTables(j.table1, tableList)
            findAlTables(j.table2, tableList)
        } else {
            tableList.add(table)
        }
    }

    /**
     * Filter all join conditions of the given table or join clause.
     *
     * @param table input table or join to scan for conditions.
     * @param clauses a list to add all found clauses.
     * @param joinStr join string when multiple clauses being add.
     */
    static void filterAllJoinConditions(Table table, List<Object> clauses, String joinStr) {
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

    /**
     * Expand given set of items to the provided list.
     *
     * @param list list to be manipulated.
     * @param items items to expand.
     */
    static void expandToList(List list, Object... items) {
        if (items == null) {
            return
        }
        for (def item : items) {
            if (item instanceof List) {
                List subList = (List)item
                for (Object obj : subList) {
                    expandToList(list, obj)
                }
            } else {
                list.add(item)
            }
        }
    }

    /**
     * Create a parameter based on the provided parameters.
     *
     * @param name parameter name.
     * @param scope scope of the parameter for named param.
     * @param mappingName mapping name of named param.
     * @return newly created parameter.
     */
    static AParam createParam(String name, AParam.ParamScope scope=null, String mappingName=null) {
        if (scope == null && mappingName == null) {
            new AParam(__name: name)
        } else {
            new NamedParam(__name: name, scope: scope, __mappingParamName: mappingName)
        }
    }

    /**
     * Creates a parameter placeholder id for a given parameter name by appending '::'.
     *
     * @param name name of parameter.
     * @return padded parameter name.
     */
    static String padParamList(String name) {
        P_PAD + name + P_PAD
    }

}
