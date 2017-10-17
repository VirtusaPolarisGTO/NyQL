## Select Query
Select query takes below form.

__Notes:__
  * `TARGET` is mandatory.
  * `FETCH` must be placed after defining `TARGET` and `JOIN` (if exist).

```groovy
$DSL.select {

     // main table to fetch data, or the table which starts joining chain, if join exist.
     TARGET (Album.alias("alb"))

     // A list of joining tables
     JOIN (TARGET()) {
         INNER_JOIN (Song.alias("s")) ON (alb.id, s.album_id)
     }

     // which columns to fetch (projection). If this is empty, then all columns will be fetched
     FETCH ()

     // conditions to select
     WHERE {
           EQ (s.genre, STR("HipHop"))
          // see WHERE sub section for more details...
     }

     // group by clause
     GROUP_BY (alb.year)
     HAVING {
            // conditions of single grouped record. Similar to content of WHERE clause
     }

     // set of columns to order
     // default assumes ASC order.
     ORDER_BY (DESC(alb.year))

    // fetch only top 10 columns
    // can use shortcut TOP to get very top N rows
     LIMIT 10 OFFSET 0
}
```

### FETCH Clause
This is the clause for selecting columns from a select query.

```groovy
FETCH (...[column | constant | parameter | function])
```

There can have special cases when fetching columns. See below for some of scenarios and how you can handle it.

#### Fetching all columns of a table
Sometimes you need to fetch all columns in a particular table (defined with an alias) whenever there
are multiple tables have been defined inside the query. To have those columns you must
specify `ALL()` for the table alias. See below example.

```groovy
$DSL.select {
    TARGET (Film.alias('f'))
    
    JOIN {
        INNER_JOIN (Actor.alias('ac')) ON f.film_id, ac.film_id
        INNER_JOIN (Role.alias('r')) ON r.role_id, ac.role_id
    }
    
    // call ALL for the table alias to get all columns of that table
    // Here we are getting all columns of Film table
    FETCH (f.ALL(), ac.actor_id, r.role_id)
}
```




#### Fetching the same column with different aliases
For some reasons, you may want to fetch same column in two different aliases. Prior to NyQL v2.0, you must do it like below.

```groovy
FETCH (
    ...
    table.my_column.alias("firstAlias"),
    table.COLUMN_AS("my_column", "secondAlias"),
    ...
)
```

However, since v2, you can implicitly use different aliases without using `COLUMN_AS` function.

```groovy
FETCH (
    ...
    table.my_column.alias("firstAlias"),
    table.my_column.alias("secondAlias"),
    ...
)
```

**WARN:** Still you can't use the same column, one without an alias at all and other with an alias, as shown in below.

```groovy
FETCH (
    // this DOES NOT WORK!!! 
    // You must specify an alias for the first-time column reference as well
    table.my_column,
    table.my_column.alias("secondAlias"),
    ...
)
```


#### Referencing column aliases in different places of query
Sometimes the alias you used in `FETCH` clause needs to be referenced in different place of the query, such as, in `GROUP_BY` or `ORDER_BY` clause.
In that case, you have to use NyQL special __`ALIAS_REF`__ function to refer the column associated
with provided alias.

`ALIAS_REF` function is very useful when your alias has whitespaces or special characters which you can directly
refer within DSL.

```groovy
$DSL.select {
    // ...
    FETCH (t.id, t.column.alias("my aliased column")
    
    // ...
    GROUP_BY (t.id, ALIAS_REF("my aliased column"))
}
```

Or alternatively you can use the column directly in place like below.

```groovy
$DSL.select {
    // ...
    FETCH (t.id, t.column.alias("my aliased column")
    
    // You can use t.column and NyQL will replace it with 'my aliased column'.
    GROUP_BY (t.id, t.column)
}
```

Above both DSL generates the same correct query.

### HAVING Clause
Same as `WHERE` clause conditions, but as you know you can use aggregated functions as well since this is being used along with SQL Group By clause.
Also you may use column aliases here as well.

Eg:
```groovy
GROUP_BY (song.year)

HAVING {

    // check number of songs per year, and filter records only if at least N songs released
    GTE (COUNT(), PARAM("minSongsPerYear"))    // COUNT() >= ?

    // assuming 'songCount' is the alias used in FETCH clause for COUNT()...
    GT (songCount, PARAM("minSongsPerYear"))
}
```
