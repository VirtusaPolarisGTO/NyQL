/**
 * @author IWEERARATHNA
 */
[
    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id, SUM(ac.income), COUNT(ac.performances))
    },
    "SELECT ac.id, SUM(ac.income), COUNT(ac.performances) FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id, SUM(ac.income).alias("actorIncome"), COUNT(ac.performances))
    },
    "SELECT ac.id, SUM(ac.income) AS actorIncome, COUNT(ac.performances) FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (IFNULL(ac.middleName, STR("")))
    },
    "SELECT CASE WHEN ac.middleName IS NULL THEN \"\" ELSE ac.middleName END FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (IFNOTNULL(ac.middleName, STR("")))
    },
    "SELECT CASE WHEN ac.middleName IS NOT NULL THEN \"\" ELSE ac.middleName END FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (IFNULL(ac.middleName, STR("")).alias("correctMiddleName"))
    },
    "SELECT CASE WHEN ac.middleName IS NULL THEN \"\" ELSE ac.middleName END AS correctMiddleName FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (CASE { WHEN { EQ (ac.middleName, STR("Kosala")) } THEN { STR("replaced") } })
    },
    "SELECT CASE WHEN ac.middleName = \"Kosala\" THEN \"replaced\" END FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (CASE {
                WHEN { EQ (ac.middleName, STR("Kosala")) AND (EQ (ac.birthYear, 1985)) } THEN { STR("replaced") }
                ELSE { STR("deleted") }
            }
        )
    },
    "SELECT CASE WHEN ac.middleName = \"Kosala\" AND ac.birthYear = 1985 THEN \"replaced\" ELSE \"deleted\" END FROM `Actor` ac",
]