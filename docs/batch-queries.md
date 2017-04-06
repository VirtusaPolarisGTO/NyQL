### Batch Queries
You can insert large batch of data at once by declaring the `$DSL.bulkInsert` syntax.
For update queries, use `$DSL.bulkUpdate`.

You can use the same set of clauses used in `$DSL.insert` syntax except whats in select insert queries.
Also, you can use only [parameters](docs/parameters.md) and constant values for query values.

```groovy
$DSL.bulkInsert {
  // ... all insert syntax will be available here
}
```

For updates:
```groovy
$DSL.bulkUpdate {
  // ... all update syntax will be available here
}
```

At runtime it expects a variable containing java list of hashmaps, which a record is equivalent to a single map. 
Each entry in the map will contain a parameter name and value.

The data you want to insert/update must be send under `__batch__` key in __$SESSION__ object.


```java
// list of records to be insered
// this could be a result of another query.
List<Map<String, Object>> records = ...

// this can only have a single variable, or if multiple, then the variable name must be equal to ''batch''
Map<String, Object> data = new HashMap<>();
data.put("__batch__", records);

NyQL.execute("<bulk-script-name>", data);
```

#### Example:

Say I want to add three songs to a table using bulk insert. Assuming the field `id` is an auto-increment
value, so that I don't want to specify in in query.

```groovy
$DSL.bulkInsert {
    
    TARGET (Song.alias("s"))
    
    DATA ([
        title:      PARAM("title"),
        year:       PARAM("year"),
        length:     PARAM("lengthInSeconds"),
        deleted:    BOOLEAN(false)          // by default set it false
    ])
}
```

From Java,

```java
List<Map<String, Object>> threeRecords = new LinkedList<>();

// 1st song
Map<String, Object> song1 = new HashMap<>();
song1.put("title", "Lose Yourself");
song1.put("year", 2002);
song1.put("lengthInSeconds", 320);

// 2nd song
Map<String, Object> song1 = new HashMap<>();
song1.put("title", "Baby One More Time");
song1.put("year", 1998);
song1.put("lengthInSeconds", 211);

// 3rd song
Map<String, Object> song1 = new HashMap<>();
song1.put("title", "I want it that way");
song1.put("year", 1999);
song1.put("lengthInSeconds", 254);

threeRecords.add(song1);
threeRecords.add(song2);
threeRecords.add(song3);

// this can only have a single variable, or if multiple, then the variable name must be equal to ''batch''
Map<String, Object> data = new HashMap<>();
data.put("__batch__", threeRecords);

NyQL.execute("<bulk-script-name>", data);
```