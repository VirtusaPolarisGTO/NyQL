### Common Table Expressions (CTE)

**WARN**: CTEs are supported only for below versions or above.
 * MySQL v8.0+
 * MariaDB v.10.2+
 * Postgres v9.1+
 * SQL Server 2008+
 * Oracle 
 
If your product has lesser version requirements, __do not__ use these NyQL constructs.

---

Common Table Expressions are very useful in self-joins, multiple references to a same subquery, and importantly
fetching recursive structures from database. CTEs are written using `WITH` clause and this is going to be supported since NyQL v2.

#### NyQL Syntax for Non-recursive CTE:

```groovy
$DSL.cte {

    /**
     * Table to derive and keep in memory for the main query.
     */
    WITH ("Table", ["columns"...]) {
        // select subquery for derived table
    }
    
    /**
     * The normal query to execute using derived table. 
     */
    SELECT {
        // select query clauses
    }
}
```

 * As indicated in query both table name and columns must be strings.
 * You can use multiple `WITH` clauses within same `$DSL.cte`
 * Specifying array of columns is optional.

See examples in [tests/scripts/cte/withq.groovy](tests/scripts/cte/withq.groovy) file.


#### NyQL Syntax for Recursive CTE:

Recursive query has three main parts. 
 1. Anchor - represents base query result. (`ANCHOR` clause)
 2. Recursion query - represents query to run on top of base query. (`RECURSION` clause)
 3. Main query

```groovy
$DSL.cte {

    /**
     * Derived recursive table.
     */
    WITH_RECURSIVE ("Table", ["columns"...]) {
        ANCHOR {
            // selection query
        }
        
        RECURSION {
            // recursion query
        }
    }
    
    /**
     * The normal query to execute using derived table. 
     */
    SELECT {
        // select query clauses
    }
}
```

See examples in [tests/scripts/cte/withrq.groovy](tests/scripts/cte/withrq.groovy) file.