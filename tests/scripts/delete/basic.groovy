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
        [mysql: "DELETE FROM `Film` WHERE `Film`.film_id = 1234",
            pg: 'DELETE FROM "Film" f WHERE f.film_id = 1234'
        ],

        $DSL.delete {
            TARGET (Film)
            WHERE {
                EQ (Film.film_id, PARAM("fID"))
            }
        },
        [
            mysql: ["DELETE FROM `Film` WHERE `Film`.film_id = ?", ["fID"]],
            pg:    ['DELETE FROM "Film" WHERE film_id = ?', ["fID"]]
        ],
]