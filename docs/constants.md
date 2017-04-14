## Constant Values

Sometimes you may need to embed constant values inside queries. 
NyQL supports writing basic constant values as below.

  * `NUM` - for numbers
  * `BOOLEAN` - for boolean values
  * `STR` - for string literals
  
All those functions can be used inside [`FETCH` clause](query-select.md#fetch-clause) with aliases, 
or with [`DATA` clause](query-inserts.md#data-clause) when inserting.

_For eg:_ you can use,

```groovy
    FETCH (NUM(2).alias("numberTwo"))
    FETCH (STR("Yes").alias("yesOrNo"))
    FETCH (BOOLEAN(true).alias("isActive"))
```

### Numbers
Using `NUM` function you may specify any number which could be integers or decimals.

```groovy
    NUM(1)      // integer values
    NUM(0)
    
    NUM(2.3)    // decimal values
    
    NUM(1234567890123456)   // long values
```

### Strings
Using `STR` function you may specify any string literal in a query.
NyQL automatically will use db-specific string quotes when generating query.

```groovy
    STR("hello world")         
    
    STR('my text')         // you may also use single quotes to represent string
```

### Booleans
Using `BOOLEAN` function you may specify constant boolean values in q query.
NyQL automatically converts appropriate representation when the query is generated.

```groovy
    BOOLEAN(true)         
    BOOLEAN(false) 
```

