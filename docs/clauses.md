## WHERE
Contains set of conditions for a query. 

#### Notes:
* Every where clause should be separated by `AND` or `OR` unless it is a grouped condition.
* Two grouped conditions are supported. `ALL {...}` and `ANY {...}`
* Use grouped conditions if you want to append parenthesis around several clauses.

#### Supported Operators
 * **EQ** : Check for equality (=)
 * **NEQ** : Not equality (!=, or <>)
 * **GT** : Greater than (>)
 * **GTE** : Greater than or equal (>=)
 * **LT** : Less than (<)
 * **LTE** : Less than or equal (<=)
 * **ISNULL** : Check for null (`IS NULL`)
 * **NOTNULL** : Check for not null (`IS NOT NULL`)
 * **IN** : Check for in (`IN`)
 * **NIN**: Check for not in (`NOT IN`)
 * **LIKE** : String like comparator (`LIKE`)
 * **NOTLIKE** : String not like comparator (`NOT LIKE`)
 * **BETWEEN** : Between operator (`BETWEEN`)
 * **NOTBETWEEN**: Not between operator (`NOT BETWEEN`)
 * **EXISTS**: EXISTS operator which returns true if sub-query contains at least one record.
 * **NOTEXISTS**: Negation of EXISTS operator.
 
Eg:
```groovy
WHERE {

    // check for equality of two columns/values
    EQ (album.year, song.year)
    AND
    EQ (album.year, PARAM("minYear"))

    // null checkings
    ISNULL (album.details)
    NOTNULL (album.details)

    // use ON to have custom operator
    GT(album.year, song.year) 
    OR
    LIKE (album.name, STR("%love%"))

    // grouped condition. you don't need AND clauses inside here.
    // nyql will automatically append 'AND' between every clauses.
    ALL {
        EQ (album.year, song.year)
        EQ (album.name, PARAM("albumName"))
    }

    // you may import where clause part from another reusable script
    $IMPORT ("<script-id>")
}
```

#### Special Operators (ANY/SOME, ALL)
SQL has `ANY` and `ALL` operators which has a different meaning with respect to NyQL's ANY and ALL grouping operators.
[See Here](https://www.w3schools.com/sql/sql_any_all.asp) for more information.

NyQL supports these conditional operators too. 
Considering readability, it is better to write inline queries inside ALL/ANY.
Also remember always use ALL/ANY as right operand of the condition.
See below examples.

```groovy

WHERE {
    GT (t.column, ANY(QUERY { 
                            // ... inner query goes here
                            }))
    OR 
    
    GT (t.column, ALL(QUERY { 
                            // ... inner query goes here
                            }))
}

```

Or you can define your query in somewhere else and use the groovy definition variable inside ANY/ALL.

```groovy

def innerQuery = $DSL.select {
    // your inner query definition
}

$DSL.select {
    // ...
    
    WHERE {
        GT (t.column, ALL(innerQuery))
    }
}
```

## TARGET

Target indicates the main table which get affected from the query. Usually there is one.
In case if you are having multiple tables, just use the table reference which you may think
should come immediately after _FROM_ clause, as the target.

Target takes one parameter and it must be a table reference. Usually it indicates the primary table of the query depending on the query type.

**Note: Make sure you always have an alias for the table**

Eg:
```groovy
TARGET (Album.alias("alb"))

TARGET (TABLE("lowercase_table").alias("ltable"))
```

## JOIN
Allows joining multiple tables at once. 

#### Notes:
 * The first parameter of JOIN clause is the first table in the joining chain.
 * The `ON` condition is same as `WHERE` clause and you may use multiple conditions enclosing in curly brackets.
 * You may also use parameters inside `ON` conditions.
 * A right side table of a join may be an inner query (See [advanced](advanced) section to see how its done)
 * You may import common joining tables from another script using `$IMPORT` function

#### Supported join types:
  * INNER_JOIN
  * [ LEFT | RIGHT ]_JOIN
  * [ RIGHT | LEFT ]_OUTER_JOIN

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

