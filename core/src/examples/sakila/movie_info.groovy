/**
 * @author IWEERARATHNA
 */
$DSL.select {

    TARGET (Film.alias("f"))

    JOIN {
        TARGET() INNER_JOIN $IMPORT("partials/movie_join")
    }

    FETCH ()

    WHERE {
        EQ (f.film_id, PARAM("filmId"))
    }
}