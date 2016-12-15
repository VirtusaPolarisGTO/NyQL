package com.virtusa.gto.nyql.db;

import com.virtusa.gto.nyql.Column;
import com.virtusa.gto.nyql.Table;

import java.util.*;

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

    public String tableMapName(Table table) {
        return tableMappings.getOrDefault(table.get__name(), table.get__name());
    }

    public String columnMapName(Table table, Column column) {
        if (columnMappings.containsKey(table.get__name())) {
            return columnMappings.get(table.get__name()).getOrDefault(column.get__name(), column.get__name());
        } else {
            return column.get__name();
        }
    }

    public static TranslatorOptions empty() {
        return new TranslatorOptions(Collections.unmodifiableList(new ArrayList<>()));
    }
}
