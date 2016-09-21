# NyQL


* Query Repository: A folder containing all your query scripts.
* Mapper: An instance responsible of mapping an identifier to a valid source which is consumable by any repository.
* QResultProxy: Contains a database specific generated query with parameters in correct order
* QScript: Executable entity contains a QResultProxy instance with relevant data
* Executor: Entity which executes script(s)

1. Add the dependency `nyql-engine` to the maven project. _Note: Use the correct version._
```xml
<dependency>
    <groupId>com.virtusa.gto.insight.nyql</groupId>
    <artifactId>nyql-engine</artifactId>
    <version>${nyql.version}</version>
</dependency>
```
2. Make sure to have correct driver classes in your classpath at the runtime through maven dependencies. For eg, if you are using mysql database use mysql jdbc driver.
```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.36</version>
</dependency>
```
3. NyQL uses a configuration property file. See a sample here. It has below properties.
    * **translators**: a comma separated fully qualified class names of db translators. These will be loaded at the beginning and will throw exception if not found.
    * **activate**: the active database implementation name. This must be equal to a name returned by any translator.
    * **cache.raw.scripts**: Whether to cache compiled dsl scripts in memory. Default value is `yes`.
    * **cache.queries**: Indicates to cache generated db specific query strings to prevent redundant translation of same query again and again. Default value is `yes`.
3. If you want to see the db specific query, then use below code piece.
```java
public class Main {
    
    public static void main(String[] args) {
        // call this once in application lifecycle to configure
        Properties configs = ... // load nyql property file
        Quickly.configOnce(configs);
        
        // load the scripts directory
        File srcDir = new File("path_to_query_directory");
        QScript result = Quickly.parse(srcDir, "<script-id>");
        
        // print db specific query to the console
        System.out.println(result.getProxy().getQuery());
        // print order of parameters according to the above query string
        System.out.println(result.getProxy().getOrderedParameters());
    }
    
}
```
4. If you want to execute a script then use below code piece. (Assuming you are using default JDBC executor to get the result)
```java
public class Main {
    
    public static void main(String[] args) throws Exception {
        // call this once in application lifecycle to configure
        Properties configs = ... // load nyql property file
        Quickly.configOnce(configs);
        
        // prepare a map containing parameter data for the running script
        Map<String, Object> data = new HashMap<>();
        data.put("minRentals", 25);
        data.put("customerId", 2);
        data.put("filmId", 250);
        
        // create script executor using a JDBC connection instance
        QExecutor executor = new QJdbcExecutor(connection);
        
        // load the scripts directory
        File srcDir = new File("path_to_query_directory");
        QScript result = Quickly.execute(srcDir, "<script-id>", data, executor);
        
        // print results. By default it returns a list of hashmaps
        if (result instanceof List) {
            for (Object row : (List)result) {
                System.out.println(row.toString());
            }
        }
    }
    
}
```

 * If you did not specify an executor, then it will try to load a `jdbc.properties` file from classpath to create a connection.