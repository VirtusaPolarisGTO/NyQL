/**
 * @author IWEERARATHNA
 */
$DSL.select {

    TARGET (Film.alias("f"))

    JOIN {
        TARGET() INNER_JOIN $IMPORT("partials/movie_join") INNER_JOIN Fork.alias("fk")
    }

    FETCH ()

    WHERE {
        EQ (f.film_id, PARAM("filmId"))
    }
}