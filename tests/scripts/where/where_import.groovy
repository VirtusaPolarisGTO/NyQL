/**
 * @author IWEERARATHNA
 */
/**
 * @author IWEERARATHNA
 */
[
        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH ()
            WHERE {
                EQ (f.film_id, 123)
                AND
                $IMPORT ("where/import_wpart")
            }
        },
        ["SELECT * FROM `Film` f " +
                "WHERE f.film_id = 123 AND f.rental_duration = ? AND f.replacement_cost >= 20.0",
         ["duration"]],

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH ()
            WHERE {
                EQ (f.film_id, 123)
                AND
                $IMPORT_UNSAFE ("where/import_wpartxxx")
            }
        },
        "SELECT * FROM `Film` f WHERE f.film_id = 123 AND",
]