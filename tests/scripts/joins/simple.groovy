/**
 * @author IWEERARATHNA
 */
[
        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                INNER_JOIN (Film_Actor.alias("fa")) ON (f.film_id, fa.film_id)
                LEFT_JOIN (Actor.alias("a")) ON (fa.actor_id, a.actor_id)
            }
            FETCH ()
        },
        [
            mysql: "SELECT * FROM `Film` f " +
                "INNER JOIN `Film_Actor` fa ON f.film_id = fa.film_id " +
                "LEFT JOIN `Actor` a ON fa.actor_id = a.actor_id"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN {
                INNER_JOIN (Film_Actor.alias("fa")) ON (f.film_id, fa.film_id)
                LEFT_JOIN (Actor.alias("a")) ON (fa.actor_id, a.actor_id)
            }
            FETCH ()
        },
        [
                mysql: "SELECT * FROM `Film` f " +
                        "INNER JOIN `Film_Actor` fa ON f.film_id = fa.film_id " +
                        "LEFT JOIN `Actor` a ON fa.actor_id = a.actor_id"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                LEFT_OUTER_JOIN (Film_Actor.alias("fa")) ON { EQ(f.film_id, fa.film_id) }
                RIGHT_JOIN (Actor.alias("a")) ON "fa.actor_id = a.actor_id"
            }
            FETCH ()
        },
        [
            mysql: "SELECT * FROM `Film` f " +
                "LEFT OUTER JOIN `Film_Actor` fa ON f.film_id = fa.film_id " +
                "RIGHT JOIN `Actor` a ON fa.actor_id = a.actor_id"
        ],

        $DSL.select {
            TARGET (TABLE("Film").alias("f"))
            JOIN (TARGET()) {
                RIGHT_OUTER_JOIN (Film_Actor.alias("fa")) ON {
                    EQ(f.film_id, fa.film_id)
                    AND
                    EQ (f.film_id, fa.second_film_id)
                }
                JOIN (Actor.alias("a")) ON "fa.actor_id = a.actor_id"
            }
            FETCH ()
        },
        [
            mysql: "SELECT * FROM `Film` f " +
                "RIGHT OUTER JOIN `Film_Actor` fa ON f.film_id = fa.film_id AND f.film_id = fa.second_film_id " +
                "INNER JOIN `Actor` a ON fa.actor_id = a.actor_id"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                $IMPORT ("joins/import_join")
            }
            FETCH ()
        },
        [
            mysql: "SELECT * FROM `Film` f INNER JOIN `Actor` ac ON f.actor_id = ac.actor_id"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                $IMPORT ("joins/import_anyjoin")
            }
            FETCH ()
        },
        [
            mysql: "SELECT * FROM `Film` f " +
                "INNER JOIN `Actor` ac ON f.actor_id = ac.actor_id " +
                "RIGHT JOIN `Payment` p ON p.actor_id = ac.actor_id"
        ],
]