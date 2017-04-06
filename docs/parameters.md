## Using Parameters
Parameters are like placeholders to inject values at runtime when executing the query. Use keyword `PARAM` or `PARAMLIST` to define a parameter in a query.

**Note:** It is **mandatory** to provide values for all parameters at the runtime if you want to successfully execute a query.

Parameters can be defined in below query clauses.
  * Fetch clauses (when you need to fetch constant values)
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
     * This type of parameter will be used mostly in `IN` queries where you will provide a list of values.
    * At runtime, NyQL will automatically inject all parameter values to the JDBC executor.
    * The actual value for this kind of parameter must be a java instance of `List`. Should
    not be a `Set` or any other unordered collection type.
    
Eg: Declaring a parameter:
```groovy
    // here actual film id value would be an integer in $SESSION object
    EQ (f.film_id, PARAM("filmId"))       // f.film_id = ?
```

Eg: For in query parameters:
```groovy
    IN (f.languages, PARAMLIST("languageList"))  // f.languages IN (?, ?, ?)
```

### Special Parameters with Auto-conversions

NyQL always tries to keep the integrity of input parameters (what user gives as input will be sent to to jdbc as it is), but
there are situations where you want to do implicit conversions of parameter values before they
are being set as JDBC parameters. For instance, converting java dates / timestamps, binary data etc.

To support such conversions, NyQL provides several additional parameter syntax. See below.
 * **PARAM_TIMESTAMP("paramName", [format])** : Converts the parameter value to `java.sql.Timestamp` instance just before
 assigning as a JDBC parameter. User can send different types as to this parameter values, such as,
    * If user has sent a long value, then NyQL assumes it is in _epoch milliseconds_.
    * If user has sent a timestamp as string, NyQL assumes it is in [ISO 8601 Timestamp](https://en.wikipedia.org/wiki/ISO_8601) format.
    * If user wants to use a custom timestamp format, user needs to specify the format he/she wishes along with the parameter name.
       * Eg: `PARAM_TIMESTAMP("paramName", "yyyy-MM-DD HH:mm:ss")`
 
 * **PARAM_DATE("paramName")** : Converts the parameter value to `java.sql.Date` instance. Here parameter
 value __must__ be in format `YYYY-MM-DD`, because there is no other way to represent a date as string.
 
 * **PARAM_BINARY("paramName")** : Converts incoming binary value to a `BinaryArrayInputStream` automatically based on receiving data type.
   * If user has sent a byte array `byte[]`, then it will be converted to a BinaryArrayInputStream.
   * If user has sent a string, NyQL assumes it is in Base64 encoded format. And that string will be
   converted to a proper stream.