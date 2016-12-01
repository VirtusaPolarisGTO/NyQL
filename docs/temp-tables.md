## DDL
Basic DDL is supported by NyQL. Specially for working with temporary tables.

Again DDL commands can only be used inside scripts. i.e. `$DSL.script { ... }`.

**Note:** It is mandatory to use all DDL commands scoped under `ddl { ... }` clause. It gives clear separation of DML and DDL.

#### Create a foreign key
Creates a foreign key with a name and a field in this table.

##### Properties of a foreign index
* __refTable__: other referencing table name
* __refFields__: list of field names in the other referenced table
* __onDelete__: what to do when deleting a record. (default: NO_ACTION)
* __onUpdate__: what to do when updating a record. (default: NO_ACTION)

Eg: Foreign key for a field _album_id_  in the _Song_  table to the _Album_  table

```groovy
FOREIGN_KEY ("fx_song_album", "album_id", [ refTable: "Album", refFields: ["id"] ])
```

Eg: Foreign key for a field _album_id_  in the _Song_  table to the _Album_  table having SET_NULLeffect when deleting and CASCADEwhen updating.

```groovy
FOREIGN_KEY ("fx_song_album", "album_id", [ refTable: "Album", refFields: ["id"], onDelete: DReferenceOption.SET_NULL, onUpdate: DReferenceOption.CASCADE ] )
```

#### Create an index
Index must have a name and field(s).

##### Properties of a index
* __unique__: is the index represents a unique index (default: false)
* __fields__: list of field names for the index (default: [])
* __indexType__: type of index. Can only be **BTREE** or **HASH**. (default: database default)

Eg: Index for a field __'name'__

```groovy
INDEX ("idx_song_name", [fields: ["name"]])
```

Eg: Index for a multiple fields.

```groovy
INDEX ("idx_song_name_title", [fields: ["name", "title"]])
```

Eg: Hash index for a field __'title'__

```groovy
INDEX ("idx_song_name", [fields: ["title"], indexType: DKeyIndexType.HASH])
```

#### Create a field
Every field must have a name and a type. Other field properties are optional and assumes below default values.

Syntax:  ` FIELD ( $fieldName, $fieldType, [$fieldProperty1: $propertValue1, $fieldProperty2: $propertValue2, ... ]) `

##### Properties of a field
* __sequence__: should this field be an auto increment field (default: false)
* __notNull__: allows null values (default: false)
* __length__: if the field type requires a length, then specify it here. (default: 0)
* __unsigned__: if the field value is numeric, mark it as unsigned. (default: false)
* __defaultValue__: default value of the field, if no value is provided when a record is inserting. (default: None)

Eg: Create an auto increment field

```groovy
FIELD("album_id", DFieldType.INT, [sequence: true, notNull: true, unsigned: true])
```

Eg: Field with a name and type

```groovy
FIELD("title", DFieldType.TEXT)
```

Eg: Field with text type having maximum length up to 256 characters.

```groovy
FIELD("details", DFieldType.TEXT, [length: 256])
```

#### Create a primary key
Primary key a special index for a table.

Eg: Primary key for field __'id'__

```groovy
PRIMARY_KEY ("id")
```

Eg: Primary key for combining multiple fields __'id'__ and __'name'__.

```groovy
PRIMARY_KEY ("id", "name")
```

#### Creating a table
You can create a table using `TABLE` function, or create a temporary table using `TEMP_TABLE` function.

Eg:
```groovy
$DSL.script {

    // start ddl execution
    ddl {
        // create a table named 'Artist'
        TABLE("Artist") {
            // create fields, indexes, and foreign keys here
        }

        // create a temporary table names 'MyTempTable'. Remember to drop table at the end of transaction.
        TEMP_TABLE("MyTempTable") {
            // create fields and indexes
        }
    }
}
```

#### Dropping a table
To drop a table use `DROP_TEMP_TABLE` function.

Eg:
```groovy
$DSL.script {

    // start ddl execution
    ddl {
        // create a temporary table names 'NameOfTempTable'. 
        TEMP_TABLE("NameOfTempTable") {
            // create fields and indexes
        }
    }

    // do some stuff here

    // drop the created temp table
    ddl {
        DROP_TEMP_TABLE ("NameOfTempTable")
    }
}
```

