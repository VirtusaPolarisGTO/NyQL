## NyQL Changelog

### v2
 * __Breaking Changes__
   * NyQL static class access `NyQL` has been removed. Migrate all parses and executions to
  a `NyQLInstance`, and bind it to your application lifecycle.
   * Function `$IMPORT_UNSAFE` has been renamed to `$IMPORT_SAFE`
   * Renamed functions `CURTIME`, `CURDATE`, and `CUREPOCH` to `CURRENT_TIME`, `CURRENT_DATE`, and `CURRENT_EPOCH` respectively.
   * Recommended to change db-inconsistent `CONCAT` functions to `CONCAT_NN` for null-ignoring concatenation.
   
 * New database supports
   * MariaDB [#28](https://github.com/VirtusaPolarisGTO/NyQL/issues/28)
   * H2 [#33](https://github.com/VirtusaPolarisGTO/NyQL/issues/33)
   
 * Features & Enhancements
    - Syntactic sugar for upsert and insertOrLoad queries [#18](https://github.com/VirtusaPolarisGTO/NyQL/issues/18)
    - Simulate FULL OUTER JOIN in unsupported databases [#24](https://github.com/VirtusaPolarisGTO/NyQL/issues/24)
    - Ability to recompile a script without having to restart JVM [#29](https://github.com/VirtusaPolarisGTO/NyQL/issues/29)
    - Provide loading scripts from multiple directories [#36](https://github.com/VirtusaPolarisGTO/NyQL/issues/36)
    - Convert given list of values to a table [#34](https://github.com/VirtusaPolarisGTO/NyQL/issues/34)
    - Provide syntactic sugar to create temp tables on the fly with selected data from another table [#25](https://github.com/VirtusaPolarisGTO/NyQL/issues/25)
    - Application level pagination for SELECT queries [#46](https://github.com/VirtusaPolarisGTO/NyQL/issues/46)
    - Application level transaction handling [#47](https://github.com/VirtusaPolarisGTO/NyQL/issues/47)
    - Raw query parts can be written with parameters [#49](https://github.com/VirtusaPolarisGTO/NyQL/issues/49)
    - Query level logging enhancements [#35](https://github.com/VirtusaPolarisGTO/NyQL/issues/35)
    - Insert syntax supports for `SET {}` instead of `DATA([])` clause
    - Ability to specify length when casting [#41](https://github.com/VirtusaPolarisGTO/NyQL/issues/41)
    - Support statistical functions [#21](https://github.com/VirtusaPolarisGTO/NyQL/issues/21)
    - Simulate java's lastIndexOf functionality in SQL queries [#20](https://github.com/VirtusaPolarisGTO/NyQL/issues/20)
    - Dev mode new script addition should be able to detect automatically [#10](https://github.com/VirtusaPolarisGTO/NyQL/issues/10)
    - Change the model to support execution of multiple parsed queries at once in executor [#19](https://github.com/VirtusaPolarisGTO/NyQL/issues/19)
    - Database version aware translators [#27](https://github.com/VirtusaPolarisGTO/NyQL/issues/27)
    
 * Bug fixes
   - Inconsistency behavior of CONCAT functions in difference databases [#30](https://github.com/VirtusaPolarisGTO/NyQL/issues/30)
   - Should not have alias when already aliased column is used inside a CASE statement [#31](https://github.com/VirtusaPolarisGTO/NyQL/issues/31)
   - PARAM() is missing when it is inside LCASE() [#38](https://github.com/VirtusaPolarisGTO/NyQL/issues/38)
   - PARAM() is missing when it is inside a THEN clause [#39](https://github.com/VirtusaPolarisGTO/NyQL/issues/39)
   - Cannot use $IMPORT_UNSAFE outside queries [#37](https://github.com/VirtusaPolarisGTO/NyQL/issues/37)
   
### v1.1.3
 * Features & Enhancements
   - Relative script paths [#13](https://github.com/VirtusaPolarisGTO/NyQL/issues/13)
   - Make TARGET() table by default for starting tables in JOIN closures [#14](https://github.com/VirtusaPolarisGTO/NyQL/issues/14)
   - Support sql ANY/SOME and ALL conditional operators [#12](https://github.com/VirtusaPolarisGTO/NyQL/issues/12)
 
 * Bug fixes
   - Cross joins does not work [#15](https://github.com/VirtusaPolarisGTO/NyQL/issues/15)
   - INSERT queries with inline QUERY does not correctly identify parameters [#11](https://github.com/VirtusaPolarisGTO/NyQL/issues/11)
   - INSERT queries, which specified as RETURN_KEYS, will never return keys if cached [#9](https://github.com/VirtusaPolarisGTO/NyQL/issues/9)
   - Cannot use HAVING clause without GROUP BY clause [#16](https://github.com/VirtusaPolarisGTO/NyQL/issues/16)
   
### v1.1.2
 * [Features and Bug fixes](https://github.com/VirtusaPolarisGTO/NyQL/issues?q=is%3Aissue+milestone%3Av1.1.2+is%3Aclosed)

#### v1.1.1
 
 * [Bug fixes](https://github.com/VirtusaPolarisGTO/NyQL/issues?q=is%3Aissue+milestone%3Av1.1.1+is%3Aclosed)


#### v1.1
    All batch related operations accept its data within the map in key name of '__batch__'
    instead of previously used key 'batch'.

  * Features
    * New result object [`NyQLResult`](docs/nyresult.md) having friendly methods to deal with the output of a query/script.
    * Ability to configure NyQL using environment variables at startup time (see Readme file for more [info](README.md#configuration-values-as-runtime-properties)).
    * New `batchUpdate` query (similar to batchInsert)
    * Binary value input/output support through base64 encoded values
    
  * Enhancements
    * Easily configure NyQL programmatically using only minimum required parameters.
    * Ability to provide non-plaintext password using `passwordEnc` in configuration
    * Readable functions for CURRENT_TIME, CURRENT_DATE, CURRENT_EPOCH instead of CURTIME, CURDATE, CUERPOCH respectively.
    * Timestamp and date parameters can be converted automatically through `PARAM_TIMESTAMP` and `PARAM_DATE`.  
    * Transactions can be written using Java API.
      
  * Bug fixes:
    * Automatically quotes identifiers/aliases if they are equal to any reserved keywords.
    * Randomly occurring JVM bytecode validation error in eclipse IDE.
    * Native query with query parameters.
    * Some string functions accepts constant values / jdbc parameters as function argument(s)
    
#### 1.0
  * Initial release