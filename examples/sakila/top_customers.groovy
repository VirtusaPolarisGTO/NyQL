/**
 * @author IWEERARATHNA
 */
def q1 = $DSL.select {

    TARGET (Film.alias("f"))

    FETCH (f.film_id, f.special_features)

    /*
    WHERE {
        EQ (f.special_features, null) AND {EQ (f.film_id, 234) AND EQ (f.special_features, STR("ssss"))}
        OR
        LIKE (f.film_id, STR("%isisis%"))
    }
    */

    ORDER_BY (f.film_id)
    LIMIT 5 OFFSET 0
}

def q2 = $DSL.select {

    TARGET (Film.alias("f"))

    FETCH (f.film_id, f.special_features)

    /*
    WHERE {
        EQ (f.special_features, null) AND {EQ (f.film_id, 234) AND EQ (f.special_features, STR("ssss"))}
        OR
        LIKE (f.film_id, STR("%isisis%"))
    }
    */

    ORDER_BY (f.film_id)
    LIMIT 5 OFFSET 2
}

$DSL.union (q1, q2)