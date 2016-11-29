## NyQL Changelog


#### 1.1-SNAPSHOT
  * Features
    * Introduction of new result object `NyQLResult` having friendly methods to deal with query result output.
    * Ability to configure NyQL using environment variables at startup time (see Readme file for more [info](l#configuration-values-as-runtime-properties)).
    * New `batchUpdate` query (similar to batchInsert)
    
  * Enhancements
    * Ability to provide non-plaintext password using `passwordEnc` in configuration
    * Readable functions for CURRENT_TIME, CURRENT_DATE, CURRENT_EPOCH instead of CURTIME, CURDATE, CUERPOCH respectively.
    * Timestamp and date parameters can be converted automatically through `PARAM_TIMESTAMP` and `PARAM_DATE`.  
      
  * Bug fixes:
    * Randomly occurring JVM bytecode validation error in eclipse IDE.
    * Native query with query parameters.
    
#### 1.0
  * Initial release