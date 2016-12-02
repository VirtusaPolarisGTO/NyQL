/**
 * @author IWEERARATHNA
 */
[
        $DSL.delete {
            TARGET (Film)
            JOIN (TARGET()) {
                INNER_JOIN (Actor) ON Actor.id, Film.id
            }
            WHERE {
                EQ (Film.film_id, 1234)
            }
        },
        [
            mysql:  "DELETE `Film` FROM `Film` INNER JOIN `Actor` ON `Actor`.id = `Film`.id WHERE `Film`.film_id = 1234",
            pg:     ""
        ],

        $DSL.delete {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                INNER_JOIN (Actor.alias("ac")) ON ac.id, f.id
            }
            WHERE {
                EQ (f.film_id, 1234)
            }
        },
        [
            mysql:  "DELETE f FROM `Film` f INNER JOIN `Actor` ac ON ac.id = f.id WHERE f.film_id = 1234",
            pg:     ""
        ]

]