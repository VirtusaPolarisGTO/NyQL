/**
 * @author IWEERARATHNA
 */
[
        $DSL.bulkDelete {
            TARGET (Film.alias("f"))
            WHERE {
                EQ (f.film_id, PARAM("filmId"))
            }
        },
        [
                mysql: ["DELETE FROM `Film` WHERE `Film`.film_id = ?",["filmId"]],
                pg: ['DELETE FROM "Film" f WHERE f.film_id = ?',["filmId"]]
        ]

]