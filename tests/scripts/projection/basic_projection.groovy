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
        FETCH ((sc.id + 4), (sc.total / 3).alias("expColumn"))
    },

    "SELECT sc.id + 4, sc.total / 3 AS expColumn FROM `Actor` ac",
]