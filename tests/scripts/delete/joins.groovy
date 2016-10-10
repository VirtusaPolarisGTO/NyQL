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
        "DELETE `Film` FROM `Film` INNER JOIN `Actor` ON `Actor`.id = `Film`.id WHERE `Film`.film_id = 1234"

]