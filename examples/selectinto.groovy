/**
 * Simple select into query.
 *
 * @author IWEERARATHNA
 */
$DSL.select {

    TARGET (Album.alias("alb"))

    INTO (MyOtherTable.alias("mo"),
            mo.col1,
            mo.col2,
            mo.col3)

}