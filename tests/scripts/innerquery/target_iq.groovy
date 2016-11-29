/**
 * @author IWEERARATHNA
 */

def innQ = $DSL.select {
    TARGET (Film.alias("f"))
    FETCH ($IMPORT("innerquery/import_fetch"))
    WHERE {
        EQ (f.film_id, PARAM("filmId"))
    }
}

[
    $DSL.select {
        TARGET (TABLE(innQ).alias("ac"))
        FETCH ()
    },
    [
        mysql: ["SELECT * FROM (SELECT f.id, f.year FROM `Film` f WHERE f.film_id = ?) ac", ["filmId"]]
    ],

    $DSL.select {
        TARGET (TABLE($IMPORT("innerquery/other_query")).alias("iq"))
    },
    [
        mysql: "SELECT * FROM (SELECT * FROM `Film` f) iq"
    ],

    $DSL.select {
        TARGET (TABLE($IMPORT("innerquery/other_query")).alias("iq"))
    },
    [
        mysql:  "SELECT * FROM (SELECT * FROM `Film` f) iq"
    ],

    $DSL.select {
        TARGET (TABLE(innQ).alias("user"))
        FETCH (user.name, user.year.alias("release"))
    },
    [
            mysql: ["SELECT `user`.name, `user`.year AS `release` FROM (SELECT f.id, f.year FROM `Film` f WHERE f.film_id = ?) `user`", ["filmId"]]
    ],
]