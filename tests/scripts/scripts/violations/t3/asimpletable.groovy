/**
 * @author Isuru Weerarathna
 */
$DSL.select {
    TARGET (Film.alias("f"))

    WHERE {
        EQ (f.id, PARAM("filmId"))
        EQ (f.year, $SESSION.year)
    }
}