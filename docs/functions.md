## Supported SQL Functions

**Note:** NyQL supports only functions which are available in all popular relational databases
and can be manipulated either directly or indirectly. Here indirect means, the function should be able to
manipulate within a single sql statement, may be using help of other functions.
 
 * `[]` indicates optional arguments.

### CASE Function
NyQL supports writing `CASE` function similar to what SQL queries have.

```groovy
CASE {
    WHEN { ... }
    THEN { ... }
    ELSE { ... }
}
```

 * `WHEN` clause is same as to the content of [`WHERE` clause](clauses.md#where).
 * `THEN`/`ELSE` clause should contain what should be returned. Usually this contains one statement.
 You may select one parameter/column/constant.

__Note:__
 Considering simplicity, NyQL supports `IFNULL` and `IFNOTNULL` function, which is a special shorthand 
 for returning value based on column value is `null` or not.
 
```groovy
IFNULL (column, <value-if-null>)

IFNOTNULL (column, <value-if-not-null>)
```

Equivalent CASE query would be,

```groovy
// IFNULL query
CASE {
    WHEN { ISNULL(column) }
    THEN { <value-if-null> }
    ELSE { column }
}

// IFNOTNULL query
CASE {
    WHEN { NOTNULL (column) }
    THEN { <value-if-not-null> }
    ELSE { column }
}
```

__Hint:__ If you want to deal with `null` values, may be [COALESCE](#other-functions) function would be helpful.

### Arithmetic Functions

NyQL provides functions for basic arithmetic operations, but you can use simple expression-way
_only_ if the first (left) operand is a column. The simple binary operator would support in below format.

```groovy
[database-column] (+-*/%) [any]

// eg: you can write/use arithmetic operators as long as
// your left operand is a column type. 
// Otherwise, use below functions.
Album.rating + 1 AS newRating
```

  * ADD (_column-1, column-2, ..._ ) 
  * MINUS (_left-operand, right-operand_ )
  * MULTIPLY (_left-operand, right-operand_ )
  * DIVIDE (_numerator, denominator_ )
  * MODULUS (_column, divide-by-value_ ) - Equivalent to `column % value`.
  * INVERSE (_column_ ) - Equivalent to  `1 / column`.

#### Bit Operators
  * BITAND (_left-operand, right-operand_ )
  * BITOR (_left-operand, right-operand_ )
  * BITXOR (_left-operand, right-operand_ )
  * BITNOT (_left-operand, right-operand_ )
  
### Sorting Function
 
| Function | Details |  Generated Query (Eg:) |
|---|---| --- |
|ASC (_column_ ) |  Ascending order of given column | _film.releaseDate_   ASC
|DESC (_column_ ) |  Descending order of given column | _film.releaseDate_   DESC
 
### Aggregation Functions
 
| Function | Details |  Generated Query (Eg:) |
|---|---| ---|
|COUNT ([_column_ ]) | Count of all grouped columns | COUNT(days) |
|MAX ([_column_ ]) | Maximum value from all records in given column | MAX(trafficViolations) |
|MIN ([_column_ ]) | Minimum value from all records in given column | MIN(temperature) |
|AVG ([_column_ ]) | Average value of all records in given column | AVG(battingRuns) |
|SUM ([_column_ ]) | Sum value of all records in given column | SUM(wickets) |
|DISTINCT ([_column_ ]) | Distinct of all records in given column | DISTINCT(players) |

### String Functions

| Function | Details |
|---|---|
|CONCAT (_column-1, column-2, ..._ ) |  Concat the set of given columns/strings. **WARN:** `NULL` ignorance is dependent on the running database. |
|CONCAT_NN (_col-1, col-2, ..._ ) | Concat the set of columns ignoring _NULL_ columns/values automatically. |
|CONCAT_WS (_separator, col-1, col-2, ..._ ) | Concat the set of columns using given separator |
|LCASE (_column_ ) | Converts string to lowercase |
|LEFT_PAD (_column_, _length_, [_text_]) | Left pad given column to the specified length using given text. If text is undefined, then will be used a space. |
|LEFT_TRIM (_column_) | Left trim whitespace from column |
|LEN (_column_ ) |  Length of the string |
|POSITION (_column, stringToFind_ ) |  Find the position of substring in the given string __(case sensitive)__. If the text is not found, returns 0. Position value is always >= 1.  |
|POSITION_LAST (_column, stringToFind_ ) |  Find the last occurred position of substring in the given string __(case sensitive)__. If the text is not found, returns 0. Position value is always >= 1.  |
|REPEAT (_column_, _noOfTimes_) | Repeats the string given amount of time |
|REVERSE (_column_) | Reverse a string |
|RIGHT_PAD (_column_, _length_, [_text_]) | Right pad given column to the specified length using given text. If text is undefined, then will be used a space. |
|RIGHT_TRIM (_column_) | Right trim whitespace from column |
|STR_LEFT (_column_, _length_) | Capture specified length from the given string starting from left |
|STR_REPLACE (_column, from, to_ ) | Replace a string of all _from_ text to _to_. Both _from_ and _to_ are 1-based indices. |
|STR_RIGHT (_column_, _length_) | Capture specified length from the given string starting from right |
|SUBSTRING (_column, startFrom, [length]_ ) | Capture part of a string starting from given location to the given length. _startFrom_ must be >= 1, because databases uses 1-based indices. |
|TRIM (_column_ ) | Trims whitespace from the column |
|UCASE (_column_ ) | Converts string to upper case |

### Math Functions

| Function | Details |
|---|---|
|ABS (_column_ )  | Gets the absolute value of a real number |
|ACOS (_value_ )  | Returns the arc cosine for given value. The value must be between -1 and 1. |
|ASIN (_value_ )  | Returns the arc sine for given value. The value must be between -1 and 1. |
|ATAN (_value_ )  | Returns the arc tangent for given value. |
|ATAN2 (_x, y_ )  | Returns the arc tangent of two variables. Equivalent to arc tangent of _x / y_. |
|CEIL (_column_ )  | Gets the ceiling value of decimal number |
|COS (_radians_ )  | Returns the cosine for given radians value. |
|COT (_value_ )  | Returns the cotangent for given value. Equivalent, _1 / TAN(x)_. |
|DEGREES (_column_ )  | Converts given radian value to degrees |
|EXP (_power_ )  | Returns value of _**e**_ raised to the given power. |
|FLOOR (_column_ )  | Gets the floor value of decimal number |
|LOGE (_value_ )  | Returns the natural logarithm of given value. |
|LOG (_base, value_ )  | Returns logarithm of _value_ to the given _base_.  |
|POWER (_column_, _magnitude_ )  | Raise the given column value to the given magnitude. ( i.e. (column)^(magnitude) ) |
|RADIANS (_column_ )  | Converts given degree value to radians |
|ROUND (_column, decimalPlaces_ ) | Round the given number to specified decimal places |
|SIGN (_column_ )  | Returns the sign value as an integer of the given column value (-1,0,+1) |
|SIN (_value_ )  | Returns the sine value of given radians. |
|SQRT (_column_ )  | Returns the square root value of the given column |
|TAN (_value_ )  | Returns the tangent for the given value. |
|TRUNCATE(_column, d_ ) | Truncate the given decimal number to _d_ decimal places. This does not _round_ the number. Negative _d_ values will truncate before the decimal point. |


### Stats Functions

| Function | Details |
|---|---|
|STDDEV_POP (_column_ ) | Population standard deviation of values in given column. |
|STDDEV_SAMP (_column_ ) | Sample standard deviation of values in given column. |
|VAR_POP (_column_ ) | Population variance of values in given column. |
|VAR_SAMP (_column_ ) | Sample variance of values in given column. |
 

### Casting Functions

| Function | Details |
|---|---|
|CAST_INT (_column ) | Cast given column value to a integer |
|CAST_STR (_column ) | Cast given column value to a string |
|CAST_DATE (_column ) | Cast given column value to a date |


### Date/Time Functions

| Function | Details |
|---|---|
|NOW ()|  Current timestamp with both date and time |
|CURRENT_DATE () | Current date without time |
|CURRENT_TIME () | Current time without date |
|DATE_TRUNC (_column_ ) | Truncate a date time to date by removing time part |
|DATE_ADD_YEARS (_startDate, by_ ) | Add given number of year to the start date |
|DATE_ADD_MONTHS (_startDate, by_ ) | Add given number of months to the start date |
|DATE_ADD_DAYS (_startDate, by_ ) | Add given number of days to the start date |
|DATE_ADD_WEEKS (_startDate, by_ ) | Add given number of weeks to the start date |
|DATE_ADD_HOURS (_startDate, by_ ) | Add given number of hours to the start date |
|DATE_ADD_MINUTES (_startDate, by_ ) | Add given number of minutes to the start date |
|DATE_ADD_SECONDS (_startDate, by_ ) | Add given number of seconds to the start date |
|DATE_DIFF_YEARS (_startDate, endDate_ ) | Difference between two dates in years |
|DATE_DIFF_MONTHS (_startDate, endDate_ ) | Difference between two dates in months |
|DATE_DIFF_DAYS (_startDate, endDate_ ) | Difference between two dates in days |
|DATE_DIFF_WEEKS (_startDate, endDate_ ) | Difference between two dates in weeks |
|DATE_DIFF_HOURS (_startDate, endDate_ ) | Difference between two dates in hours |
|DATE_DIFF_MINUTES (_startDate, endDate_ ) | Difference between two dates in minutes |
|DATE_DIFF_SECONDS (_startDate, endDate_ ) | Difference between two dates in seconds |
|DATE_SUB_YEARS (_startDate, by_ ) | Subtract given number of year to the start date |
|DATE_SUB_MONTHS (_startDate, by_ ) | Subtract given number of months to the start date |
|DATE_SUB_DAYS (_startDate, by_ ) | Subtract given number of days to the start date |
|DATE_SUB_WEEKS (_startDate, by_ ) | Subtract given number of weeks to the start date |
|DATE_SUB_HOURS (_startDate, by_ ) | Subtract given number of hours to the start date |
|DATE_SUB_MINUTES (_startDate, by_ ) | Subtract given number of minutes to the start date |
|DATE_SUB_SECONDS (_startDate, by_ ) | Subtract given number of seconds to the start date |

### Epoch Related Functions
 * __Note:__ All databases, by default, provides up to epoch seconds, __not to milliseconds__ like programming languages does. However NyQL accepts and assumes user provides milliseconds, and below functions are for those.

| Function | Details |
|---|---|
|CURRENT_EPOCH () |  Current epoch milliseconds |
|EPOCH_TO_DATE (_column_ )|  Convert epoch milliseconds to date |
|EPOCH_TO_DATETIME (_column_ )|  Convert epoch milliseconds to date time |

### Other Functions

| Function | Details |
|---|---|
|COALESCE (_columns_, ...) |  Returns first non-null value |
|GREATEST (_val1, val2_ ) | Returns the highest values from given two values. This is equivalent to `CASE WHEN val1 >= val2 THEN val1 ELSE val2 END`. |
|LEAST (_val1, val2_ ) | Returns the highest values from given two values. This is equivalent to `CASE WHEN val1 <= val2 THEN val1 ELSE val2 END`. |
