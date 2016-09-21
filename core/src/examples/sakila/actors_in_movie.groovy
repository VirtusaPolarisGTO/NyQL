/**
 * @author IWEERARATHNA
 */
$DSL.select {

    TARGET (Actor.alias("ac"))

    JOINING {
        Film.alias("f") \
            INNER_JOIN Film_Actor.alias("fa")   ON f.film_id, fa.film_id \
            INNER_JOIN ac                       ON fa.actor_id, ac.actor_id
    }

    FETCH (ac)

    WHERE {
        ON (f.title, LIKE(STR("%dragon%")))
    }
}