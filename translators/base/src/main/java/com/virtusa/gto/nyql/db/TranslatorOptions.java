package com.virtusa.gto.nyql.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class TranslatorOptions {

    private final Collection<String> keywords;
    private final Map<String, String> tableMappings;
    private final Map<String, Map<String, String>> columnMappings;

    public TranslatorOptions(Collection<String> keywords) {
        this.keywords = keywords;
        this.tableMappings = Collections.unmodifiableMap(new HashMap<>());
        this.columnMappings = Collections.unmodifiableMap(new HashMap<>());
    }

    public TranslatorOptions(Collection<String> keywords, Map<String, String> tableMappings, Map<String, Map<String, String>> columnMappings) {
        this.keywords = keywords;
        this.tableMappings = tableMappings;
        this.columnMappings = columnMappings;
    }

    public Collection<String> getKeywords() {
        return keywords;
    }

    public String tableMapName(String tblName) {
        return tableMappings.getOrDefault(tblName, tblName);
    }

    public String columnMapName(String table, String column) {
        if (columnMappings.containsKey(table)) {
            return columnMappings.get(table).getOrDefault(column, column);
        } else {
            return column;
        }
    }

    public static TranslatorOptions empty() {
        return new TranslatorOptions(Collections.unmodifiableList(new ArrayList<>()));
    }
}
