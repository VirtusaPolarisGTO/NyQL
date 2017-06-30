## Delete Query
Delete query accepts a target table, optional joining table(s), and a optional set of conditional clauses.

```groovy
$DSL.delete {
    // target table which we are going to delete records
    TARGET(Film.alias("f"))

    // where clause (optional)
   WHERE {
        ...
   }
}
```

### Deletes with Joins

You can use joins inside deletes.


**WARN**: Not all databases supports deletes with joins. In that case, clause `ON_UNIQUE_KEYS` becomes mandatory.
Or, considering the safer side, __do not__ use joins in deletes, but write using `EXISTS` condition.

```groovy
$DSL.delete {
    // target table which we are going to delete records
    TARGET(Film.alias("f"))

    // optional joining tables. Used for filtering out conditions
    JOIN (TARGET()) {
        INNER_JOIN (FilmActors.alias("fac")) ON (fac.film_id, f.film_id)
        INNER_JOIN (Actor.alias("ac")) ON (fac.actor_id, ac.actor_id)
    }
  
    // where clause (optional)
   WHERE {
        ...
   }
   
   // mandatory clause to specify when db does not support joins
   ON_UNIQUE_KEYS(f.col1, f.col2)
}
```

**Note**: `ON_UNIQUE_KEYS` clause is optional, but mandatory in databases which does not support delete joins.
It indicates columns which is/are unique to the main target table, so the generated/derived query will use those column to select rows for deletion when joins exist.

