/**
 * A select query finding all actors of each film,
 * ordered by film, actor's last name and then first name.
 *
 * @author IWEERARATHNA
 */
$DSL.select {

    TARGET (Film.alias("f"))

    JOIN (TARGET()) {
        INNER_JOIN (Film_Actor.alias("fa"))   ON f.film_id, fa.film_id
        INNER_JOIN (Actor.alias("ac"))   ON fa.actor_id, ac.actor_id

    }

    FETCH (f.film_id, ac)

    WHERE {
        LIKE (f.title, STR("%dragon%"))
    }

    ORDER_BY (f.film_id, ac.last_name, ac.first_name)
}