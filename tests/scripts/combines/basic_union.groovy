/**
 * @author IWEERARATHNA
 */
def q1 = $DSL.select {
    TARGET (Film.alias("f"))
    FETCH (f.film_id, f.title)
}

def q2 = $DSL.select {
    TARGET (ForeignFilms.alias("ff"))
    FETCH (ff.film_id, ff.title)
}

[
    $DSL.union (q1, q2),
    "(SELECT f.film_id, f.title FROM `Film` f) UNION ALL (SELECT ff.film_id, ff.title FROM `ForeignFilms` ff)",

    $DSL.unionDistinct (q1, q2),
    "(SELECT f.film_id, f.title FROM `Film` f) UNION (SELECT ff.film_id, ff.title FROM `ForeignFilms` ff)"
]