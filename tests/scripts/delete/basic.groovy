/**
 * @author IWEERARATHNA
 */
[
        $DSL.delete {
            TARGET (Film.alias("f"))
            WHERE {
                EQ (f.film_id, 1234)
            }
        },
        [mysql: "DELETE FROM `Film` WHERE `Film`.film_id = 1234"],

        $DSL.delete {
            TARGET (Film)
            WHERE {
                EQ (Film.film_id, 1234)
            }
        },
        [mysql: "DELETE FROM `Film` WHERE `Film`.film_id = 1234"],
]