## Update Query

Update query takes below form.

__Notes:__
  * `TARGET` is mandatory.
  * You can use only `EQ` and `SET_NULL` under `SET` clause.

```groovy
$DSL.update {

     // main table which update rows
     TARGET (Song.alias("s"))

     // the fields to set
     SET {
          EQ (s.year, album.year)
          SET_NULL (s.details)
     }

     // conditions to select records
     WHERE {
          GT (alb.year, 2015)   // alb.year > 2015
     }

}
```

### Updates with Joins

You can add `JOIN` clause within update query.
 
 **WARN**:
 Be careful, not all databases supports joining inside an update query.
 In such case, it is better to use `EXISTS` condition within where clause having joining as a subquery. See internet for many references
 on how to write such queries.
 

```groovy
$DSL.update {
    
     // optional clause for update-joins
     JOIN (TARGET()) {
          LEFT_OUTER_JOIN Album_Songs.alias("albsong") ON (s.id, albsong.song_id)
          INNER_JOIN Album.alias("alb") ON (alb.id, albsong.album_id)
     }

}
```