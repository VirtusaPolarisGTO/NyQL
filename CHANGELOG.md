## NyQL Changelog

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