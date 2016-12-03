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

### Special Parameters with Auto-conversions

NyQL always tries to keep the input parameter integrity (what user gives input to jdbc), but
there are situations where you want to do implicit conversions of parameter values before they
are being set as JDBC parameters. For instance, converting java dates / timestamps, binary data etc.

To support such conversions NyQL provides additional parameter syntax. See below.
 * **PARAM_TIMESTAMP** : Converts the parameter value to `java.sql.Timestamp` instance just before
 assigning as a JDBC parameter. User can send different types as to this parameter values.
    * If user has sent a long value, then NyQL assumes it is in _epoch milliseconds_.
    * If user has sent a timestamp as string, NyQL assumes it is in [ISO 8601 Timestamp](https://en.wikipedia.org/wiki/ISO_8601) format.
    * If user wishes to use custom timestamp format, user needs to specify the format he/she wishes along with the parameter name.
       * Eg: `PARAM_TIMESTAMP("paramName", "yyyy-MM-DD HH:mm:ss")`
 
 * **PARAM_DATE** : Converts the parameter value to `java.sql.Date` instance. Here parameter
 value must be in format `YYYY-MM-DD` because there is no other way to represent date as string.
 
 * **PARAM_BINARY** : Converts incoming binary value to a `BinaryArrayInputStream` automatically based on receiving data type.
   * If user has sent a byte array `byte[]`, then it will be converted to a BinaryArrayInputStream.
   * If user has sent a string, NyQL assumes it is in Base64 encoded format. And that string will be
   converted to a proper stream.