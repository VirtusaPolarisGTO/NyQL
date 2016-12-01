## Using Parameters
Parameters are like placeholders to inject values at runtime when executing the query. Use keyword `PARAM` to define a parameter in a query.

**Note:** It is **mandatory** to provide values for all parameters at the runtime if you want to successfully execute a query.

Parameters can be defined in below query clauses.
  * Conditions in joins
  * Where clause
  * Value assignments in INSERT query
  * Value assignments in UPDATE query
  * Having clause
  * LIMIT and OFFSET values

There are two types of parameters.
  * Normal single value parameters.  `PARAM`
     * This is the most common usage of parameters for almost all clauses. It contains a single value.
  * Multi value parameters. `PARAMLIST`
     * This type of parameter will be used mostly in `IN` queries where you will provide list of values.
    * At runtime, NyQL will automatically inject parameters to the JDBC executor.

Eg: Declaring a parameter:
```groovy
    EQ (f.film_id, PARAM("filmId"))       // f.film_id = ?
```

Eg: For in query parameters:
```groovy
    IN (f.languages, PARAMLIST("languageList"))  // f.languages IN (?, ?, ?)
```