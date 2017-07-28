### Upsert Queries
Upsert query is a set of queries which executes sequentially to create/update a record if it does not already exist in databases.
This is a syntactic sugar, because currently some databases are not supporting a syntax to upsert a record to a table natively.
Closest thing databases have is the `MERGE` syntax.

Previously you have to write a `script` to handle _create-if-not-exist-or-update-otherwise_ query type.
Now you can convert a `UPDATE` query to UPSERT query in an instant, by changing only query type to `upsert` from `update`.

The importance of this `UPSERT` query would be it is __cacheable__ unlike the script.

**Note:** All upsert and insertOr queries are running inside a transaction implicitly.

**Note:** Both `SET` and `WHERE` clauses are mandatory in a upsert query.

**WARN:** Never use the upserts as bulk/batch inserts or updates since a single upsert query generates about 3 or 4 queries and can lead 
to very inefficient execution.

#### Execution Steps:
 * First based on the database, it will search whether there is any native supported query for upsert.
 * If such native query exist, then it will be generated and executed.
 * Otherwise, behind the script, NyQL will generate a set of queries which will be run sequentially, as shown in below.
     * The `WHERE` clause will be used to first select the record from database.
       * `TARGET` and `JOIN` clauses will be used too.
       * Executed query will return only __at most__ one record.
     * If there are any records exist, NyQL will automatically execute the update query.
     * If no records exist, NyQL will execute an insert query to add a record to the main table as specifed in `TARGET` table.
       * NyQL uses `SET` clause to set column values for the new record.
       * Does not use tables in `JOIN` clauses at all.
     * Based on return type specified by user, NyQL will return either record before updating, record after updating,
     or custom projected record.

#### Returning Types
There are four returning types you can specify in the query itself to say what you want as a result after the execution.
*Note:* NyQL will never send `null` as a result, it will always send a [NyQLResult](nyresult.md) instance.

1. __RETURN_BEFORE__: Returns the record before the update happens. If inserted, empty `NyQLResult` returned.
2. __RETURN_AFTER__: Returns the record after the update or newly inserted record.
3. __RETURN_COLUMNS__: If you want to return the record after the update or insert having only custom set of columns, use this option.
    * Syntax: *RETURN_COLUMNS(columns, ...)* 
4. __RETURN_NONE__ (default): Returns an empty `NyQLResult` always.

#### Examples

Eg-01:
Below query will update or insert a record into table and returns the updated or inserted record.
```groovy
$DSL.upsert {
    TARGET (Film.alias("f"))
    SET {
        EQ (f.film_id, 1234)
        EQ (f.title, PARAM("title"))
        SET_NULL (f.language_id)
    }
    WHERE {
        GT (f.year, 2010)
    }
    
    RETURN_AFTER()
}
```

Eg-02:
```groovy
$DSL.upsert {
    TARGET (Film.alias("f"))
    SET {
        EQ (f.film_id, 1234)
        EQ (f.title, PARAM("title"))
        SET_NULL (f.language_id)
    }
    WHERE {
        GT (f.year, 2010)
    }
    
    // final result send to user contains only film_id 
    // and title in the updated record.
    RETURN_COLUMNS (f.film_id, f.title)
}
```


### InsertOrLoad Query
Very similar to upsert query, but this **does not** perform an update in case a record already exist in the database.
It simply returns that record to the user. Syntax is `$DSL.insertOrLoad { }`.

**Note:** You cannot specify a **Return type** fot this kind of query, because it always returns
the fresh record exist in the database. If you specify return type, query will fail.

Eg-01:
```groovy
$DSL.insertOrLoad {
    TARGET (Film.alias("f"))
    SET {
        EQ (f.film_id, 1234)
        EQ (f.title, PARAM("title"))
        SET_NULL (f.language_id)
    }
    WHERE {
        EQ (f.year, 2010)
    }
}
```

If you write the query without `WHERE` clause, the conditions of `SET` clause will be converted 
to a WHERE clause combining with `AND`.

```groovy
$DSL.insertOrLoad {
    TARGET (Film.alias("f"))
    SET {
        EQ (f.film_id, 1234)
        EQ (f.title, PARAM("title"))
        SET_NULL (f.language_id)
    }
}
```
