/**
 * @author IWEERARATHNA
 */
[
        $DSL.select {
            TARGET (Film.alias("f"))
            INTO (OtherFilms.alias("of"), $IMPORT("inserts/into_imports"))
        },
        "INSERT INTO `OtherFilms` (`film_id`, `title`) SELECT * FROM `Film` f",



]