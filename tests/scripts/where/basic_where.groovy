/**
 * @author IWEERARATHNA
 */
[
    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            EQ (f.id, 123)
            AND
            NEQ (f.title, STR("hello"))
            AND
            IN (f.year, 2014)
            OR
            IN (f.year, PARAM("yearOf"))
        }
    },
    "SELECT * FROM `Film` f " +
            "WHERE f.id = 123 AND f.title <> \"hello\" AND f.year IN (2014) OR f.year IN (?)",

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH ()
        WHERE {
            ALL {
                EQ (f.id, 123)
                NEQ (f.title, STR("hello"))
            }
            AND
            IN (f.year, 2014)
            OR
            IN (f.year, PARAM("yearOf"))
        }
    },
    "SELECT * FROM `Film` f " +
            "WHERE (f.id = 123 AND f.title <> \"hello\") AND f.year IN (2014) OR f.year IN (?)",
]