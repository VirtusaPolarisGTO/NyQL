## NyQL Result of a Script

Since v1.1 the result of a script execution has been changed to a `NyQLResult` which
actually is a list of maps like in the previous version, but having more developer
friendly interface to deal with result data.

With the new `NyQLResult` instance, now you can directly access a field value, 
cast a column to Java compatible data types (specially `boolean`), and accessing
affected count/keys upon the execution of insert/update query.

__Note:__ For the users coming from v1.0 does not need to change anything unless
they want to take advantage of these newly introduced methods.

#### Accessing Field Value

Accessing field value is much easier. Use,
 * `getField(rowIndex, columnName)`: to get a cell/field value in a given row and column name. 
 Here rowIndex parameter must be a 0-based index.
 * `asString(rowIndex, columnName)`: Returns column value as string for the given row and column.
 * `asBool(rowIndex, columnName)`: Returns column value as boolean for the given row and column.
 * `asInt(rowIndex, columnName)`: Returns column value as integer for the given row and column.

Example Java code:

```java
NyQLResult result = nyInstance.execute("scriptId", data);

// calling getField will return Object
Object val = result.getField(0, "id");

boolean boolVal = result.asBool(1, "boolColumn");
String strVal = result.asString(1, "stringColumn");
int numVal = result.asInt(1, "intColumn");
```

Within scripts:

```groovy
$DSL.script {
    def result = RUN("scriptId");
    
    // calling getField will return Object
    def val = result.getField(0, "id")
    
    def boolVal = result.asBool(1, "boolColumn")
    def strVal = result.asString(1, "stringColumn")
    def numVal = result.asInt(1, "intColumn")
}
```

#### Changing Column Value Type

One of the painful thing with dealing result output was handling booleans, since it
depends on uder defined database schema type, or the jdbc driver. When accessing it
you had to do the conversion manually, but now NyQLResult provides several methods to
convert column values to specific java types automatically inplace of the result map.

__REMEMBER:__ NyQL does not do these value conversions automatically because it has no
idea what your value range may be like at the time of returning.

Initially supports below three mutate functions.
 * `mutateToBool(columnName)`: change column values to java boolean values.
 * `mutateToInt(columnName)`: change column values to java integer values. (in case if it returns BigInteger data types)
 * `mutateToDouble(columnName)`: change column values to java double values. (in case if it returns BigDecimal data types)

In every above occasion, the `null` values will remain `null`. And it may throw cast exceptions
if you try to convert a column which has actually a big value than the type it is converting.

#### Accessing Count and Keys

When you execute a insert/update/delete query NyQL returns total number of affected
rows and optionally affected keys (if it is an insert query). Keys are returning
_only_ if you specified `RETURN_KEYS()` in the insert query.

For single query statements, you may use:
 * `affecedCount`: returns total number of changed rows.
 * `affectedKeys`: returns list of primary keys of inserted records. 
 
For batch operation statement, keys won't be returned because JDBC drivers does not support it. You may use:
 * `affectedCounts`: returns list of total number of affected rows for each batch invocation.

Previously you had to access the count/keys like this.

```groovy
List<Map<String, Object>> result = nyInstance.execute("scriptId", data);
long count = result.get(0).get("count");
List<Integer> keys = result.get(0).get("keys");
```

In new NyQLResult instance, you can access it like,

```groovy
NyQLResult result = nyInstance.execute("scriptId", data);
long count = result.affectedCount();
List<Integer> keys = result.affectedKeys();
```

__Note:__ All of those new methods throws a `NyException` if you are accessing counts
from wrong result, or you are calling wrong method (mistaking batch and single operations).
So prepare to deal with exception too.