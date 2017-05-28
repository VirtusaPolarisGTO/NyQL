## Insert Query
There are two forms of insert query, that one is single record insertion, 
and other is insert by selecting from other table, which is explained in [next section](#select-insert-query).

```groovy
$DSL.insert {

     // target table to insert data
     TARGET (Song.alias("s"))

     // a collection of field values to set in the record using parameter values
     // see parameter section for more information
     DATA ([
          "title":  PARAM("title"),
          "artist_id":  PARAM("artist")
     ])
}
```

### DATA Clause
In INSERT query you must use key-value pairs for data in each column. 
**Note that here you must use `([ ... ])` instead of curly brackets `{}`**. The quotes are optional for column names, unless they have whitespaces.

Eg:
```groovy
DATA ([
    id: PARAM("songId"),
    title: PARAM("songTitle"),
    year: PARAM("year")
])

// in case if you are having column names with whitespaces, 
// use double quotes ("").
DATA ([
    id: PARAM("songId"),
    "album name": PARAM("albumName")
])
```

### SET Clause
_(Since v2)_ Instead of using key-value pairs, you can specify columns and their values using
`SET` clause as similar to the in [`UPDATE` query](query-update.md).

Eg:
```groovy
SET {
    EQ (s.id, PARAM("songId"))
    EQ (s.title, PARAM("songTitle"))
    EQ (s.year, PARAM("year"))
}
```


## Select Insert Query
Sometimes you might want to insert records into a table, selecting from another table.
For that, you can use syntax very similar to `select` syntax, but additionally with `INTO` clause,
which describes the target and columns in the destination table. 

Within `INTO` clause, the first argument is the target table name and it must have an alias.
Subsequent arguments are the columns, in order, which you want to insert fetched columns.

The order of the columns in `FETCH` clause and `INTO` clause must match with their data types. 
Otherwise you will get runtime errors.

Again `INTO` clause must be placed after `TARGET` and `JOIN` clauses.

```groovy
$DSL.insert {

     // you can use every clause in select query here
     // FETCH clause become mandatory.
     // ...

     // specify the destination table name to insert all selected rows
     // you must specify all column names in projection order here after table name
     INTO (LastYearSongs.alias("la"), la.id, la.title, la.year)

}
```

If you want to insert constant values to the target table, use [parameters](parameters.md) or inline [constants](constants.md).

```groovy
$DSL.insert {
    
    TARGET(Songs.alias("s"))
    
    FETCH (s.id, s.title, s.year)

    INTO (LastYearSongs.alias("la"), 
            la.id, la.title, la.year, 
            PARAM("runtimeValue"),      // parameter value which coming at runtime
            NUM(30)                     // constant number value
        )

}
```