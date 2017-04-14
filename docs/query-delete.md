## Delete Query
Delete query accepts a target table, optional joining table(s), and a optional set of conditional clauses.

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
}
```
