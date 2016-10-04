/**
 * @author IWEERARATHNA
 */
[
        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                INNER_JOIN (Film_Actor.alias("fa")) ON (f.film_id, fa.film_id)
                INNER_JOIN (Actor.alias("a")) ON (fa.actor_id, a.actor_id)
            }
            FETCH ()
        },
        "SELECT * FROM `Film` f " +
                "INNER JOIN `Film_Actor` fa ON f.film_id = fa.film_id " +
                "INNER JOIN `Actor` a ON fa.actor_id = a.actor_id",

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                $IMPORT ("joins/import_join")
            }
            FETCH ()
        },
        "SELECT * FROM `Film` f " +
                "INNER JOIN `Actor` ac ON f.actor_id = ac.actor_id",
]