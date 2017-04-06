## Union Query
Generates union of two queries. There are two types of union, that is selecting without duplicates and with duplicates.

### Union with duplicates
Here all records will be returned.

```groovy
def query1 = $DSL.select { ... }
def query2 = $DSL.select { ... }

$DSL.union (query1, query2)
```

### Union without duplicates
Here it returns only distinct records from both queries.

```groovy
def query1 = $DSL.select { ... }
def query2 = $DSL.select { ... }

$DSL.unionDistinct (query1, query2)
```