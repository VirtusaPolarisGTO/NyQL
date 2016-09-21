/**
 * @author IWEERARATHNA
 */
$DSL.select {

    TARGET (Film.alias("f"))

    JOINING {
        TARGET() \
            INNER_JOIN (Film_Category.alias("fc")) ON f.film_id, fc.film_id \
            INNER_JOIN (TABLE("Category").alias("c")) ON c.category_id, fc.category_id
    }

    FETCH ()

    WHERE {
        EQ (f.film_id, P("filmId"))
    }
}