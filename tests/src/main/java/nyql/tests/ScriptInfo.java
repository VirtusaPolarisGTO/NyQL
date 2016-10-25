package nyql.tests;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * @author IWEERARATHNA
 */
public class ScriptInfo {

    private String name;
    private Set<String> calls;
    private Set<String> callees;
    private Set<String> tables;
    private String queryType;

    public Set<String> tableChain(Function<String, ScriptInfo> fetcher) {
        if (calls == null || calls.isEmpty()) {
            return tables;
        }
        Set<String> tbs = new HashSet<>(tables);
        for (String c : calls) {
            ScriptInfo apply = fetcher.apply(c);
            if (apply != null) {
                tbs.addAll(apply.tableChain(fetcher));
            }
        }
        return tbs;
    }

    public boolean hasTable(String tbName) {
        if (tables != null) {
            for (String tbl : tables) {
                if (tbName.equalsIgnoreCase(tbl)) {
                    return true;
                }
            }
        }
        return false;
    }

    Set<String> getCallees() {
        return callees;
    }

    void setCallees(Set<String> callees) {
        this.callees = callees;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    Set<String> getCalls() {
        return calls;
    }

    void setCalls(Set<String> calls) {
        this.calls = calls;
    }

    Set<String> getTables() {
        return tables;
    }

    void setTables(Set<String> tables) {
        this.tables = tables;
    }

    String getQueryType() {
        return queryType;
    }

    void setQueryType(String queryType) {
        this.queryType = queryType;
    }
}
