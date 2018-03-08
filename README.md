[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.virtusa.gto.nyql/nyql-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.virtusa.gto.nyql/nyql-core)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://travis-ci.org/VirtusaPolarisGTO/NyQL.svg?branch=dev)](https://travis-ci.org/VirtusaPolarisGTO/NyQL)
[![StackOverflow](https://img.shields.io/:stack%20overflow-nyql-green.svg)](http://stackoverflow.com/questions/tagged/nyql)


# NyQL

A common query DSL for popular relational databases. You can write queries or scripts using the DSL
once and execute it almost any relational database many times without
modifying it from vendor to vendor. 
NyQL removes the pain of dealing with some database specific
characteristics automatically, such as auto-quoting, type conversions, query clause positioning etc.
 
To provide that though, NyQL uses a flexible common layer to write your queries using DSL.

Currently this supports `mysql` only, but `postgres`, `mssql` and `oracle` is in progress.

#### Some Features:
  * Write queries using a DSL in database independent way making sure those work in every 
  popular relational dbs.
  * Can write queries in separate files using DSL, and no need to write or raw queries
  using string templates or any other manipulations.
  * Prevents SQL injection.
  * Your code and queries are independent, so that no need to compile your code again and
  again when a query is changed.
  * Ability to generate highly dynamic queries using groovy scripting features.
  * Query generation through reusable query parts (clauses), so, no need to copy same clauses
  again and again in every query leading to higher maintainability.
  * Queries can be cached as per your choice, so no regeneration is needed when once generated.
  * Almost same performance as you execute it directly through JDBC driver. 
  Negligible parsing overhead.

##### NyQL is not for you, if:
 * you are looking for DDL and migration activities (use [Liquibase](http://www.liquibase.org/) instead)
 * you want to use very highly database specific features, so that you have no intention of
 supporting your application over other database vendors.
 * you are looking for ORM activities through NyQL.

#### Terminology:
* `Query Repository`: A folder containing all your query scripts and responsible of parsing a nyql query and generate a native query for the activated database.
* `Mapper`: An instance responsible of mapping a script identifier to a valid source which is consumable by any repository.
* `QResultProxy`: Contains a database specific generated query with parameters in correct order
* `QScript`: Executable entity contains a QResultProxy instance with relevant data
* `Executor`: Entity which executes script(s)

#### How to Use
* Add the dependency `nyql-engine` to the maven project, and also corresponding nyql database translator.

```xml
<dependency>
    <groupId>com.virtusa.gto.nyql</groupId>
    <artifactId>nyql-engine</artifactId>
    <version>${nyql.version}</version>
</dependency>
```

If you are going to use `mysql` then add `nyql-impl-mysql` dependency to the classpath.

```xml
<dependency>
    <groupId>com.virtusa.gto.nyql</groupId>
    <artifactId>nyql-impl-mysql</artifactId>
    <version>${nyql.version}</version>
</dependency>
```

* If you are expecting to use the execution part of NyQL, then you need to specify the executor instance to be used with. By default,
NyQL works with a pooled JDBC executor, currently equipped with a [Hikari](https://github.com/brettwooldridge/HikariCP) or [C3p0](http://www.mchange.com/projects/c3p0/) pools.
For eg: if you expect to use hikari pool, (which is our recommendation) you need to add below dependency.

```xml
<dependency>
    <groupId>com.virtusa.gto.nyql</groupId>
    <artifactId>nyql-pool-hikari</artifactId>
    <version>${nyql.version}</version>
</dependency>
```

* Make sure to have correct driver classes in your classpath at the runtime through maven dependencies. For eg, if you are using mysql database use mysql jdbc driver.

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.36</version>
</dependency>
```

* NyQL uses [slf4j  logging](http://www.slf4j.org/). To enable logging, you need add appropriate slf4j implementation
to the dependency as well. Say you want to use [log4j logging](https://logging.apache.org/log4j/1.2/download.html). 
To disable logging, use slf4j Nop logger.

```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-log4j12</artifactId>
    <version>${slf4j.version}</version>
</dependency>

<dependency>
    <groupId>log4j</groupId>
    <artifactId>log4j</artifactId>
    <version>1.2.17</version>
</dependency>
```

#### Quick Start
  After you have added above dependencies, you can create a `NyQLInstance` to
  run queries like below.
  
```java 
     public class Main {
         
         public static void main(String[] args) throws Exception {
             // create a new nyql instance using minimum but
             // mandatory parameters.
             NyQLInstance nyInstance = NyQLInstance.create(new File(fileLocation));
             
             // use this retrieved instance to execute queries...
             NyQLResult result = nyInstance.execute('script-id', data);
         
            // when your application closes, call shutdown
            nyInstance.shutdown();
         }
     }
 ```
 
 Or, you can configure it without a configuration json file like below.  
  
```java 
     public class Main {
         
         public static void main(String[] args) throws Exception {
             // create a new nyql instance using minimum but
             // mandatory parameters.
             Configurations nyConfigs = NyConfig.withDefaults()
                                    .forDatabase("mysql")
                                    .scriptFolder(new File("./scripts"))
                                    .jdbcOptions("jdbc:mysql://localhost/sampledb", "username", "password")
                                    .build();
             NyQLInstance nyInstance = NyQLInstance.create(nyConfigs);
             
             // use this retrieved instance to execute queries...
         
            // when your application closes, call shutdown
            nyInstance.shutdown();
         }
     }
 ```

#### Note

* NyQL will __not__ automatically shutdown when your application closes, but if you specified jvm property
`com.virtusa.gto.nyql.addShutdownHook=true`, then NyQL will automatically add a shutdown hook for your 
initialized instance.

#### Configuration JSON

* NyQL uses a configuration json file in the classpath to configure automatically. See a sample [nyql.json](nyql.json). It has below properties.
    * **version**: `2` This property must be in all configuration files when using v2 or above.
    * **activate**: the active database implementation name. This must be equal to a name returned by any translator.
    * **caching**: 
      * **compiledScripts**: Whether to cache the groovy compiled scripts or not. Recommended to set this `true`.
      * **generatedQueries**: Whether to cache generated queries by NyQL. Then you can have fine tune by specifying a cache status for each script using `do_cache=true` declaration in very top of scripts you want to cache. Recommended to set this `true`.
      * **allowRecompilation**: _(Since v2)_ Whether to recompile scripts again at runtime. Works when _compiledScripts_ is set to `true` only.
    * **executors**: List of executors for query execution. Each executor should declare below properties.
      * **name**: name of the executor. Should be unique.
      * **factory**: factory class which creates executors at runtime for each session.
      * **url**: JDBC url
      * **username**: database username
      * **password**: database password
      * **passwordEnc**: encoded database password in base64
      * **pooling**: NyQL uses HikariCP for JDBC connection pooling. And you can specify those configurations here as a JSON object. See their [site](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby) for available configurations
    * **profiling**: Enables query profiling at runtime and emits time taken for every query invocation.
      * **enabled**: `true/false` enable/disable profiling
      * **profiler**: full class name for the profiler to activate.
      * **options**: a set of options for the profiler.

#### Configuration Values as Runtime Properties

NyQL allows specifying configuration values through environment variables using `-D` option incase if you are
expecting to execute in cloud environments or secure environments. Below shows the supported configurations.

  * **NYQL_ACTIVE_DB**: the database implementation to activate. (_activate_ property in json)
  * **NYQL_JDBC_URL**: JDBC url. 
  * **NYQL_JDBC_USERNAME**: database username.
  * **NYQL_JDBC_PASSWORD**: database password in plaintext.
  * **NYQL_JDBC_PASSWORD_ENC**: base64 encoded database password.
  * **NYQL_JDBC_DRIVER**: driver class name of the jdbc driver.
 
## Links     
    
 * [For issues or feature request, use github issue tracker](https://github.com/VirtusaPolarisGTO/NyQL/issues).
 * [For questions, ask in StackOverflow under the tag of 'nyql'](https://stackoverflow.com/questions/ask/advice?tags=nyql).
     
## Contributing

See [contributing guideline](https://github.com/VirtusaPolarisGTO/NyQL/blob/dev/CONTRIBUTING.md) for more information.
     
## LICENSE

NyQL has been released under Apache 2.0 License.