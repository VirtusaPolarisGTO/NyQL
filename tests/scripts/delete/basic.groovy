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
        "DELETE FROM `Film` f WHERE f.film_id = 1234"

]