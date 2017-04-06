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
     ORDER_BY (DESC(alb.year))

    // fetch only top 10 columns
    // can use shortcut TOP to get very top N rows
     LIMIT 10 OFFSET 0
}
```
