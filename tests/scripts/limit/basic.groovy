/**
 * @author IWEERARATHNA
 */
[
        $DSL.select {
            TARGET (Film.alias("f"))
            WHERE {
                GT (f.film_id, 100)
            }
            LIMIT 5
        },
        [
            mysql: "SELECT * FROM `Film` f WHERE f.film_id > 100 LIMIT 5"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            WHERE {
                GT (f.film_id, 100)
            }
            LIMIT 5
            OFFSET 10
        },
        [
            mysql: "SELECT * FROM `Film` f WHERE f.film_id > 100 LIMIT 5 OFFSET 10"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            WHERE {
                GT (f.film_id, 100)
            }
            LIMIT 5 OFFSET 10
        },
        [
            mysql: "SELECT * FROM `Film` f WHERE f.film_id > 100 LIMIT 5 OFFSET 10"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            WHERE {
                GT (f.film_id, 100)
            }
            TOP 10
        },
        [
            mysql: "SELECT * FROM `Film` f WHERE f.film_id > 100 LIMIT 10 OFFSET 0"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            WHERE {
                GT (f.film_id, 100)
            }
            LIMIT (PARAM("limitP"))
        },
        [
            mysql: ["SELECT * FROM `Film` f WHERE f.film_id > 100 LIMIT ?", ["limitP"]]
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            WHERE {
                GT (f.film_id, 100)
            }
            LIMIT (PARAM("limitP")) OFFSET (PARAM("offSet"))
        },
        [
            mysql: ["SELECT * FROM `Film` f WHERE f.film_id > 100 LIMIT ? OFFSET ?", ["limitP", "offSet"]]
        ],
]