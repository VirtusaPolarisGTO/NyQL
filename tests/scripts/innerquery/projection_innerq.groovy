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

def innQ2 = $DSL.select {
    TARGET (Actor.alias("ac"))
    FETCH ()
}

[
        $DSL.select {
            TARGET (Payment.alias("p"))
            FETCH (TABLE(innQ1), TABLE(innQ2))
        },
        ["SELECT (SELECT COUNT(*) AS totalFilms FROM `Film` f WHERE f.film_id = ?), " +
                 "(SELECT * FROM `Actor` ac) FROM `Payment` p",
         ["filmId"]],

        $DSL.select {
            TARGET (Payment.alias("p"))
            FETCH (TABLE(innQ1).alias("films"), TABLE(innQ2))
        },
        ["SELECT (SELECT COUNT(*) AS totalFilms FROM `Film` f WHERE f.film_id = ?) AS films, " +
                 "(SELECT * FROM `Actor` ac) FROM `Payment` p",
         ["filmId"]],

]