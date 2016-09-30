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
        FETCH (ac.id + 4, (ac.total / 3).alias("expColumn"))
    },
    "SELECT (ac.id + 4), (ac.total / 3) AS expColumn FROM `Actor` ac",
]