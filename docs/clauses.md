## DATA
In INSERT query you must use key-value pairs for data in each column. **Note that here you must use `([ ... ])` instead of curly brackets `{}`**. The quotes are optional for column names, unless they have whitespaces.

Eg:
```groovy
DATA ([
    id: PARAM("songId"),
    title: PARAM("songTitle"),
    year: PARAM("year")
])
```


## HAVING
Same as `WHERE` clause conditions, but as you know you can use aggregated functions as well since this is being used along with SQL Group By clause.

Eg:
```groovy
GROUP_BY (song.year)

HAVING {

    // check number of songs per year, and filter records only if at least N songs released
    GTE (COUNT(), PARAM("minSongsPerYear"))    // COUNT() >= ?

}
```

## WHERE
Contains set of conditions for a query. 

###### Notes:
* Every where clause should be separated by `AND` or `OR` unless it is a grouped conditions.
* Two grouped conditions are supported. `ALL {...}` and `ANY {...}`

Eg:
```groovy
WHERE {

    // check for equality of two columns/values
    EQ (album.year, song.year)
    AND
    EQ (album.year, PARAM("minYear"))

    // null checkings
    ISNULL (album.details)
    NOTNULL album.details

    // use ON to have custom operator
    GT(album.year, song.year) 
    OR
    ON (album.name, LIKE(STR("%love%")))

    // grouped conditions. you don't need AND, OR clauses inside here
    ALL {
        EQ (album.year, song.year)
        EQ (album.name, PARAM("albumName"))
    }

    // you may import where clause part from another reusable script
    $IMPORT ("<script-id>")
}
```

## TARGET
Target takes one parameter and it must be a table reference. Usually it indicates the primary table of the query depending on the query type.

**Note: Make sure you always have an alias for the table**

Eg:
```groovy
TARGET (Album.alias("alb"))

TARGET (TABLE("lowercase_table").alias("ltable"))
```

## JOIN
Allows joining multiple tables at once. 

###### Notes:
 * The first parameter of JOIN clause is the first table in the joining chain.
 * The `ON` condition is same as `WHERE` clause and you may use multiple conditions enclosing in curly brackets.
 * You may also use parameters inside `ON` conditions.
 * A right side table of a join may be an inner query (See [advanced](advanced) section to see how its done)
 * You may import common joining tables from another script using `$IMPORT` function

###### Supported join types:
  * INNER_JOIN
  * [ LEFT | RIGHT ]_JOIN
  * [ FULL | RIGHT | LEFT ]_OUTER_JOIN

Eg: If you have only one joining condition you just specify the two column names to be joining. DSL assumes they are join based on equality.
```groovy
JOIN (TARGET()) {
     INNER_JOIN Song.alias("s") ON (alb.id, s.id) 
     INNER_JOIN Artist.alias("art") ON {
                ALL {
                    NEQ (alb.id, s.id)   //  alb.id <> s.id
                    EQ (alb.sid, PARAM("songId"))   // a parameter will decide joining at execution moment.
                }
            }
}
```

Eg:
```groovy
JOIN (TARGET()) {
    $IMPORT("partials/common_joining")
}
```

Eg: You may combine your tables with imported join part. 
Make sure left most table of the imported join is equal to the right most table in the owning query, and right most table to the left most table in postfix join chain, so at runtime those two will be merged automatically. To be equal both tables must have same name and same alias.
```groovy
JOIN (TARGET()) {
    $IMPORT("partials/common_joining") 
    INNER_JOIN TABLE("OtherTable").alias("ot")
}
```

