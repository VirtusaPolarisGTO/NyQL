/**
 * @author IWEERARATHNA
 */
[
        $DSL.select {
            TARGET (Film.alias("f"))
            DISTINCT_FETCH (f.title)
        },
        "SELECT DISTINCT f.title FROM `Film` f",

        $DSL.select {
            TARGET (Film.alias("f"))
            DISTINCT_FETCH (f.title, f.description)
        },
        "SELECT DISTINCT f.title, f.description FROM `Film` f",

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (f.rental_duration, COUNT())
            GROUP_BY (f.rental_duration)
            HAVING {
                GT (COUNT(), 200)
            }
        },
        "SELECT f.rental_duration, COUNT(*) FROM `Film` f GROUP BY f.rental_duration " +
                "HAVING COUNT(*) > 200",

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (f.rental_duration, COUNT().alias("total"))
            GROUP_BY (f.rental_duration)
            HAVING {
                GT (total, 200)
            }
        },
        "SELECT f.rental_duration, COUNT(*) AS total FROM `Film` f GROUP BY f.rental_duration " +
                "HAVING total > 200",

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (COLUMN("rental_duration"), COUNT().alias("total"))
            GROUP_BY (f.rental_duration)
            HAVING {
                GT (total, 200)
            }
        },
        "SELECT f.rental_duration, COUNT(*) AS total FROM `Film` f GROUP BY f.rental_duration " +
                "HAVING total > 200",

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (CASE {WHEN { EQ (f.count, PARAM("total")) }
                THEN { BOOLEAN(true) }
                ELSE { BOOLEAN(false) }})
            WHERE {
                EQ (f.count, PARAM("total"))
            }
        },
        ["SELECT CASE WHEN f.count = ? THEN 1 ELSE 0 END FROM `Film` f WHERE f.count = ?", ["total", "total"]]

]