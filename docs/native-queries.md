## Native Queries

If you feel some function is missing or not working properly as expected and waiting until it is being fixed
by developers, or if you really hate NyQL but can't get rid of it from right now, you can write db specific
queries in scripts. At runtime, engine will pick the query and parameters for the activated database and run it
directly using a JDBC connector. Here important thing is that your query will not be changed by any means by NyQL engine
before executing.

There are two variations for specifying native queries.

First one is for the query which is common for all databases. So regardless of database kind (mysql, postgres, mssql, etc)
the same query and parameter list will be used to invoke.

```groovy
$DSL.nativeQuery(queryType, parameterOrderAsList, queryString)
```

Second one is to specify db specific queries and parameters separately.

```groovy
$DSL.nativeQuery(queryType, [ dbType: [parameterList, queryStr], dbType2: [...], ... ])
```

It is always recommended to use latter definition as it is easy to extend later.

##### Defining Parameters
 For native queries, the way of defining parameters is slightly different, as you have to prefix `$DSL` with all parameter functions.
 And for each parameter, there must be a jdbc parameter placeholder (`?`) in the query string.

```groovy
$DSL.PARAM("parameterName")

$DSL.PARAMLIST("parameterListName")
```

 Very important thing to note here is that when you specifying list parameters (i.e. `PARAMLIST`), you must use the parameter holder
 as `::<parameterName>::` format in the query. See the example in below.


##### Notes:
 * Make sure you escape all identifier names correctly in the query string according to the database. NyQL will __not__ adjust them for you in native queries.
 * For each query you must specify the type of query you are defining. (See `QueryType` enum for available types)
 * There must be an entry which is having `dbType` key equal to the currently activated database acronym.
 * Although your parameter order for all databases remain same order, you still have to specify them along with specific db query.


### Examples

A select query with a single parameter for all databases escaping table name correctly.

```groovy
$DSL.nativeQuery (
        QueryType.SELECT,
        [
            mysql: [[$DSL.PARAM("filmId")],
                    "SELECT * FROM `Film` f WHERE f.film_id = ? LIMIT 5"
            ],
            pg:    [[$DSL.PARAM("filmId")],
                    "SELECT * FROM 'Film' f WHERE f.film_id = ? LIMIT 5"
            ],
            mssql: [[$DSL.PARAM("filmId")],
                    'SELECT * FROM "Film" f WHERE f.film_id = ? LIMIT 5'
            ]
        ]
    )
```

A select query with single parameter common for all databases. Here no need to escape table name since it is not a keyword or
having whitespaces.

```groovy
$DSL.nativeQuery (
        QueryType.SELECT,
        [ $DSL.PARAMLIST("filmId") ],
        "SELECT * FROM Film f WHERE f.film_id IN ::filmId:: LIMIT 5"
    )
```

Here parameter name `filmId` must exactly be equal to the placeholder in query string. Otherwise it won't work!