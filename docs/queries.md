## Select Query
Select query takes below form.

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

## Insert Query
There are two forms of insert query, that one is single record insertion, and other is insert by selecting from other table, which is explained in [next section](#select-insert-query).

```groovy
$DSL.insert {

     // target table to insert data
     TARGET (Song.alias("s"))

     // a collection of field values to set in the record using parameter values
    // see parameter section for more information
     DATA (
          "title":  PARAM("title"),
          "artist_id":  PARAM("artist")
     )
}
```
### Bulk Insert Query
You can insert large batch of data at once by declaring the `$DSL.bulkInsert` syntax but at runtime it expects a variable containing java list of hashmaps. There a record equivalent to a single map.  For update queries, use `$DSL.bulkUpdate`.

You can use the same set of clauses used in `$DSL.insert` syntax except being used for select insert query clauses.

When you execute a query do as below to send all records to be inserted under the key `__batch__`.

```groovy
$DSL.bulkInsert {
  // ... all insert syntax will be available here
}
```

For updates:
```groovy
$DSL.bulkUpdate {
  // ... all update syntax will be available here
}
```

```java
// list of records to be insered
List<Map<String, Object>> records = ...

// this can only have a single variable, or if multiple, then the variable name must be equal to ''batch''
Map<String, Object> data = new HashMap<>();
data.put("__batch__", records);

NyQL.execute("<bulk-script-name>", data);
```

## Select Insert Query
If you want to insert into a table selecting from another table, then use the same `insert` syntax, but add the `INTO` clause.

```groovy
$DSL.insert {

     // you can use every clause used in select query
     // ...

     // specify the destination table name to insert all selected rows
    // you must specify all column names in projection order here after table name
     INTO (LastYearSongs.alias("la"), la.id, la.title, la.year)

}
```

## Update Query

```groovy
$DSL.update {

     // main table which update rows
     TARGET (Song.alias("s"))

     // optional clause for update-joins
     JOIN (TARGET()) {
          LEFT_OUTER_JOIN Album_Songs.alias("albsong") ON (s.id, albsong.song_id)
          INNER_JOIN Album.alias("alb") ON (alb.id, albsong.album_id)
     }

     // the fields to set
     SET {
          EQ (s.year, album.year)
          SET_NULL (s.details)
     }

     // conditions to select records
     WHERE {
          GT (alb.year, 2015)   // alb.year > 2015
     }

}
```

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

## Union Query
Generates union of two queries. There are two types of union, that is selecting without duplicates and with duplicates.

### Union with duplicates
Here all records will be returned.

```groovy
def query1 = $DSL.select { ... }
def query2 = $DSL.select { ... }

$DSL.union (query1, query2)
```

### Union without duplicates
Here it returns only distinct records from both queries.

```groovy
def query1 = $DSL.select { ... }
def query2 = $DSL.select { ... }

$DSL.unionDistinct (query1, query2)
```