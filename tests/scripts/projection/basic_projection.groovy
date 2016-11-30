/**
 * @author IWEERARATHNA
 */
[
    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH ()
    },
    [
        mysql: "SELECT * FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id, ac.name.alias("aliasedName"), ac.title)
    },
    [
        mysql: "SELECT ac.id, ac.name AS aliasedName, ac.title FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac)
    },
    [
        mysql: "SELECT ac.* FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (TABLE("Actor").alias("ac"))
        FETCH (ac)
    },
    [
        mysql: "SELECT ac.* FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id, ac.name.alias("aliased Name"), ac.title)
    },
    [
        mysql: "SELECT ac.id, ac.name AS `aliased Name`, ac.title FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH ((ac.id + 4), (ac.total / 3).alias("expColumn"))
    },
    [
        mysql: "SELECT (ac.id + 4), (ac.total / 3) AS expColumn FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id - 4, (ac.total / 3).alias("expColumn"))
    },
    [
        mysql: "SELECT (ac.id - 4), (ac.total / 3) AS expColumn FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id * 4, (ac.total % 3).alias("expColumn"))
    },
    [
        mysql: "SELECT (ac.id * 4), (ac.total % 3) AS expColumn FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (NUM(1).alias("constantColumn"), NUM(3))
    },
    [
        mysql: "SELECT 1 AS constantColumn, 3 FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (BOOLEAN(true).alias("constantColumn"), BOOLEAN(false))
    },
    [
        mysql: "SELECT 1 AS constantColumn, 0 FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (STR("hello").alias("constantColumn"), STR("yeah"))
    },
    [
        mysql: "SELECT \"hello\" AS constantColumn, \"yeah\" FROM `Actor` ac"
    ],

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
    [
        mysql: ["SELECT film_id, title FROM `Film` WHERE film_id = ?", ["filmId"]]
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH ()
        ORDER_BY (ASC(ac.joinYear), DESC(ac.totalPerformances))
    },
    [
        mysql: "SELECT * FROM `Actor` ac ORDER BY ac.joinYear ASC, ac.totalPerformances DESC"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.joinYear.alias("joinY"))
        ORDER_BY (ASC(joinY))
    },
    [
        mysql: "SELECT ac.joinYear AS joinY FROM `Actor` ac ORDER BY joinY ASC"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.joinYear.alias("joinY"))
        GROUP_BY (joinY)
    },
    [
        mysql: "SELECT ac.joinYear AS joinY FROM `Actor` ac GROUP BY joinY"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (PARAM("abc").alias("name"), PARAM("abc").alias("id"))
    },
    [
        mysql: ["SELECT ? AS name, ? AS id FROM `Actor` ac", ["abc", "abc"]]
    ],

    $DSL.select {
        FETCH (NUM(12345).alias("sid"))
    },
    [
        mysql: "SELECT 12345 AS sid"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.COLUMN("name").alias("nameOne"), ac.COLUMN_AS("name", "nameTwo"))
    },
    [
        mysql: "SELECT ac.name AS nameOne, ac.name AS nameTwo FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.COLUMN_AS("name", "nameOne"), ac.COLUMN_AS("name", "nameTwo"))
    },
    [
        mysql: "SELECT ac.name AS nameOne, ac.name AS nameTwo FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.name.alias("user"), ac.year.alias("release"))
        ORDER_BY (DESC(user))
    },
    [
            mysql: "SELECT ac.name AS `user`, ac.year AS `release` FROM `Actor` ac ORDER BY `user` DESC"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.name.alias("userName"))
        GROUP_BY (ALIAS_REF("userName"))
    },
    [
            mysql: "SELECT ac.name AS userName FROM `Actor` ac GROUP BY userName"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.name.alias("user name"))
        ORDER_BY (DESC(ALIAS_REF("user name")))
    },
    [
            mysql: "SELECT ac.name AS `user name` FROM `Actor` ac ORDER BY `user name` DESC"
    ],
]