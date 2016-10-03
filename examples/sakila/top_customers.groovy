do_cache=true

/**
 * @author IWEERARATHNA
 */
$DSL.select {

    TARGET (Film.alias("f"))

    FETCH (f.film_id, f.special_features)

    /*
    WHERE {
        EQ (f.special_features, null) AND {EQ (f.film_id, 234) AND EQ (f.special_features, STR("ssss"))}
        OR
        LIKE (f.film_id, STR("%isisis%"))
    }
    */

    WHERE {
        IN (f.film_id, PARAMLIST("teamIDs"))
    }

    ORDER_BY (f.film_id)

}