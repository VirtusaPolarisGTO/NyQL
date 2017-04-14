## Update Query

Update query takes below form.

__Notes:__
  * `TARGET` is mandatory.
  * You can use only `EQ` and `SET_NULL` under `SET` clause.

```groovy
$DSL.update {

     // main table which update rows
     TARGET (Song.alias("s"))

     // optional clause for update-joins
     JOIN (TARGET()) {
          LEFT_OUTER_JOIN Album_Songs.alias("albsong") ON (s.id, albsong.song_id)
          INNER_JOIN Album.alias("alb") ON (alb.id, albsong.album_id)
     }

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