## NyQL Changelog


#### 1.1-SNAPSHOT
  * Features
    * New result object `NyQLResult` having friendly methods to deal with the output of a query/script.
    * Ability to configure NyQL using environment variables at startup time (see Readme file for more [info](l#configuration-values-as-runtime-properties)).
    * New `batchUpdate` query (similar to batchInsert)
    * Binary value input/output support through base64 encoded values
    
  * Enhancements
    * Easily configure NyQL programmatically using only mandatory parameters.
    * Ability to provide non-plaintext password using `passwordEnc` in configuration
    * Readable functions for CURRENT_TIME, CURRENT_DATE, CURRENT_EPOCH instead of CURTIME, CURDATE, CUERPOCH respectively.
    * Timestamp and date parameters can be converted automatically through `PARAM_TIMESTAMP` and `PARAM_DATE`.  
      
  * Bug fixes:
    * Automatically quotes identifiers/aliases if they are equal to any reserved keywords.
    * Randomly occurring JVM bytecode validation error in eclipse IDE.
    * Native query with query parameters.
    * Some string functions accepts constant values / jdbc parameters as function argument(s)
    
#### 1.0
  * Initial release