/**
 * @author IWEERARATHNA
 */

def innQ = $DSL.select {
    TARGET (Film.alias("f"))
}

[
    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            EQ (f.film_id, 123)
            AND
            NEQ (f.title, STR("ACE GOLDFINDER"))
            AND
            IN (f.release_year, 2006)
            OR
            IN (f.language_id, PARAM("langId"))
        }
    },
    "SELECT * FROM `Film` f " +
            "WHERE f.film_id = 123 AND f.title <> \"ACE GOLDFINDER\" AND f.release_year IN (2006) OR f.language_id IN (?)",

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            ALL {
                EQ (f.film_id, 123)
                NEQ (f.title, STR("ACE GOLDFINDER"))
            }
            AND
            GT (f.release_year, 2004)
            OR
            GTE (f.rental_duration, PARAM("y2017"))
        }
    },
    "SELECT * FROM `Film` f " +
            "WHERE (f.film_id = 123 AND f.title <> \"ACE GOLDFINDER\") AND f.release_year > 2004 OR f.rental_duration >= ?",

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            ANY {
                EQ (f.film_id, 123)
                NEQ (f.title, STR("AIRPLANE SIERRA"))
            }
            AND
            LT (f.release_year, 2015)
            OR {
                LTE(f.length, PARAM("y2017"))
            }
        }
    },
    "SELECT * FROM `Film` f " +
            "WHERE (f.film_id = 123 OR f.title <> \"AIRPLANE SIERRA\") AND f.release_year < 2015 OR f.length <= ?",

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            EQ (1, 1)
            AND
            ALL {
                EQ (f.film_id, 123)
                NEQ (f.title, STR("POND SEATTLE"))
            }
            AND
            NOTNULL (f.rating)
            AND
            ISNULL (f.original_language_id)
        }
    },
    "SELECT * FROM `Film` f " +
            "WHERE 1 = 1 AND (f.film_id = 123 AND f.title <> \"POND SEATTLE\") AND f.rating IS NOT NULL AND f.original_language_id IS NULL",

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            NEQ (2, 1)
            AND
            ANY {
                EQ (f.film_id, 123)
                NEQ (f.title, STR("MOULIN WAKE"))
            }
            AND {
                LIKE(f.description, STR("%Dragon%"))
            }
            AND
            NOTLIKE (f.title, STR("%Fairy"))
        }
    },
    "SELECT * FROM `Film` f " +
            "WHERE 2 <> 1 AND (f.film_id = 123 OR f.title <> \"MOULIN WAKE\") AND f.description LIKE \"%Dragon%\" AND f.title NOT LIKE \"%Fairy\"",

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            BETWEEN (f.rental_rate, 2.99, 5.0)
            AND
            GT (f.film_id, 1)
        }
    },
    "SELECT * FROM `Film` f " +
            "WHERE f.rental_rate BETWEEN 2.99 AND 5.0 AND f.film_id > 1",

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            NOTBETWEEN (f.rental_rate, 2.99, 5.0)
            AND (NOTIN (f.language_id, 1))
        }
    },
    "SELECT * FROM `Film` f " +
            "WHERE f.rental_rate NOT BETWEEN 2.99 AND 5.0 AND f.language_id NOT IN (1)",

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            NOTBETWEEN (f.rental_rate, 2.99, 5.0)
            OR (IN (f.language_id, PARAMLIST("singleList")))
        }
    },
    "SELECT * FROM `Film` f " +
            "WHERE f.rental_rate NOT BETWEEN 2.99 AND 5.0 OR f.language_id IN (::singleList::)",

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            NOTBETWEEN (f.rental_rate, 2.99, 5.0)
            AND (IN (f.language_id, PARAMLIST("doubleList")))
        }
    },
    "SELECT * FROM `Film` f " +
            "WHERE f.rental_rate NOT BETWEEN 2.99 AND 5.0 AND f.language_id IN (::doubleList::)",

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            EQ (f.language_id, null)
            AND
            NEQ (f.language_id, null)
        }
    },
    "SELECT * FROM `Film` f " +
            "WHERE f.language_id IS NULL AND f.language_id IS NOT NULL",

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            NEQ (f.title, STR("ACE GOLDFINDER"))
            AND
            IN (f.release_year, $SESSION.emptyList)
        }
    },
    "SELECT * FROM `Film` f " +
            "WHERE f.title <> \"ACE GOLDFINDER\" AND f.release_year IN (NULL)",

    $DSL.select {
        TARGET (Address.alias("ad"))
        FETCH ()
        WHERE {
            EQ (ad.city_id, ad.city_id + 1)
        }
    },
    "SELECT * FROM `Address` ad WHERE ad.city_id = (ad.city_id + 1)",

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            LIKE (f.description, CONCAT(STR("%"), POSITION(f.title, STR("a")), STR("-%")))
        }
    },
    "SELECT * FROM `Film` f WHERE f.description LIKE CONCAT(\"%\", POSITION(\"a\" IN f.title), \"-%\")",

    $DSL.select {
        TARGET (Address.alias("ad"))
        WHERE {
            EXISTS (innQ)
            AND
            NOTEXISTS (innQ)
        }
    },
    "SELECT * FROM `Address` ad WHERE EXISTS (SELECT * FROM `Film` f) AND NOT EXISTS (SELECT * FROM `Film` f)",

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            NEQ (f.title, STR("ACE GOLDFINDER"))
            AND
            IN (f.release_year, PARAMLIST("years"))
        }
    },
    "SELECT * FROM `Film` f " +
            "WHERE f.title <> \"ACE GOLDFINDER\" AND f.release_year IN (::years::)",

    $DSL.select {
        TARGET (Film.alias("f"))
        WHERE {
            EQ (f.description, IFNOTNULL(f.alternative_title, STR("")))
        }
    },
    "SELECT * FROM `Film` f WHERE f.description = CASE WHEN f.alternative_title IS NOT NULL THEN \"\" ELSE f.alternative_title END",
]