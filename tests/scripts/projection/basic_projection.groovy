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
        FETCH (ac)
    },
    "SELECT ac.* FROM `Actor` ac",

    $DSL.select {
        TARGET (TABLE("Actor").alias("ac"))
        FETCH (ac)
    },
    "SELECT ac.* FROM `Actor` ac",

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
    ["SELECT film_id, title FROM `Film` WHERE film_id = ?", ["filmId"]],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH ()
        ORDER_BY (ASC(ac.joinYear), DESC(ac.totalPerformances))
    },
    "SELECT * FROM `Actor` ac ORDER BY ac.joinYear ASC, ac.totalPerformances DESC",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.joinYear.alias("joinY"))
        ORDER_BY (ASC(joinY))
    },
    "SELECT ac.joinYear AS joinY FROM `Actor` ac ORDER BY joinY ASC",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.joinYear.alias("joinY"))
        GROUP_BY (joinY)
    },
    "SELECT ac.joinYear AS joinY FROM `Actor` ac GROUP BY joinY",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (PARAM("abc").alias("name"), PARAM("abc").alias("id"))
    },
    ["SELECT ? AS name, ? AS id FROM `Actor` ac", ["abc", "abc"]],

    $DSL.select {
        FETCH (NUM(12345).alias("sid"))
    },
    "SELECT 12345 AS sid",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.COLUMN("name").alias("nameOne"), ac.COLUMN_AS("name", "nameTwo"))
    },
    "SELECT ac.name AS nameOne, ac.name AS nameTwo FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.COLUMN_AS("name", "nameOne"), ac.COLUMN_AS("name", "nameTwo"))
    },
    "SELECT ac.name AS nameOne, ac.name AS nameTwo FROM `Actor` ac",
]