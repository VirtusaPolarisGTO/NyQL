/**
 * @author IWEERARATHNA
 */
def innQ1 = $DSL.select {
    TARGET (Film.alias("f"))
    FETCH (COUNT().alias("totalFilms"))
    WHERE {
        EQ (f.film_id, PARAM("filmId"))
    }
}

def innQU = $DSL.select {
    EXPECT (Payment.alias("p"))

    TARGET (Film.alias("f"))
    FETCH (COUNT().alias("totalFilms"))
    WHERE {
        EQ (f.film_id, PARAM("filmId"))
        AND
        EQ (p.payment_id, f.payment_id)
    }
}

def innQ2 = $DSL.select {
    TARGET (Actor.alias("ac"))
    FETCH ()
}

[
        $DSL.select {
            TARGET (Payment.alias("p"))
            FETCH (TABLE(innQ1), TABLE(innQ2))
        },
        [
            mysql: ["SELECT (SELECT COUNT(*) AS totalFilms FROM `Film` f WHERE f.film_id = ?), " +
                    "(SELECT * FROM `Actor` ac) FROM `Payment` p",
                    ["filmId"]]
        ],

        $DSL.select {
            TARGET (Payment.alias("p"))
            FETCH (TABLE(innQ1).alias("films"), TABLE(innQ2))
        },
        [
            mysql: ["SELECT (SELECT COUNT(*) AS totalFilms FROM `Film` f WHERE f.film_id = ?) AS films, " +
                    "(SELECT * FROM `Actor` ac) FROM `Payment` p",
                    ["filmId"]]
        ],

        $DSL.select {
            TARGET (Payment.alias("p"))
            FETCH (TABLE(innQU).alias("films"), TABLE(innQ2))
        },
        [
            mysql: ["SELECT (SELECT COUNT(*) AS totalFilms FROM `Film` f WHERE f.film_id = ? AND p.payment_id = f.payment_id) AS films, " +
                    "(SELECT * FROM `Actor` ac) FROM `Payment` p",
                    ["filmId"]]
        ],
]