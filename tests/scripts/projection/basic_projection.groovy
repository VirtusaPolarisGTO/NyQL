/**
 * @author IWEERARATHNA
 */
[
    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH ()
    },
    "SELECT * FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id, ac.name.alias("aliasedName"), ac.title)
    },
    "SELECT ac.id, ac.name AS aliasedName, ac.title FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id, ac.name.alias("aliased Name"), ac.title)
    },
    "SELECT ac.id, ac.name AS `aliased Name`, ac.title FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH ((ac.id + 4), (ac.total / 3).alias("expColumn"))
    },
    "SELECT (ac.id + 4), (ac.total / 3) AS expColumn FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id - 4, (ac.total / 3).alias("expColumn"))
    },
    "SELECT (ac.id - 4), (ac.total / 3) AS expColumn FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id * 4, (ac.total % 3).alias("expColumn"))
    },
    "SELECT (ac.id * 4), (ac.total % 3) AS expColumn FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (NUM(1).alias("constantColumn"), NUM(3))
    },
    "SELECT 1 AS constantColumn, 3 FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (STR("hello").alias("constantColumn"), STR("yeah"))
    },
    "SELECT \"hello\" AS constantColumn, \"yeah\" FROM `Actor` ac",

    $DSL.select {
        if ($SESSION.trueCondition) {
            TARGET (Film)
        } else {
            TARGET (TVSeries)
        }

        FETCH (film_id, title)
        WHERE {
            EQ (film_id, PARAM("filmId"))
        }
    },
    "SELECT film_id, title FROM `Film` WHERE film_id = ?",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH ()
        ORDER_BY (ASC(ac.joinYear), DESC(ac.totalPerformances))
    },
    "SELECT * FROM `Actor` ac ORDER BY ac.joinYear ASC, ac.totalPerformances DESC",
]