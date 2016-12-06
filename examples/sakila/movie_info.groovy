/**
 * @author IWEERARATHNA
 */
$DSL.select {

    TARGET (Film.alias("f"))

    JOIN (TARGET()) {
        // you can import/reuse join part from another file if
        // the same join clauses are being joined in many queries.
        $IMPORT("sakila/partials/movie_join")
    }

    FETCH ()

    WHERE {
        EQ (f.film_id, PARAM("filmId"))
    }
}