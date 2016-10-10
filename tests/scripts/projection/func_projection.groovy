import com.sun.org.apache.bcel.internal.generic.IFNULL

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
        FETCH (ac.id, MAX(ac.income), MIN(ac.performances), AVG(ac.income))
        GROUP_BY (ac.id)
    },
    "SELECT ac.id, MAX(ac.income), MIN(ac.performances), AVG(ac.income) FROM `Actor` ac GROUP BY ac.id",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id, SUM(ac.income).alias("actorIncome"), COUNT(ac.performances))
    },
    "SELECT ac.id, SUM(ac.income) AS actorIncome, COUNT(ac.performances) FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (IFNULL(ac.middleName, STR("")))
    },
    "SELECT IFNULL(ac.middleName, \"\") FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH ((CASE {
            WHEN {
                NEQ (IFNULL(ac.income, 0) + IFNULL(ac.income2, 0), 9)
            } THEN { IFNULL(ac.income, 0) }
        }).alias("myCol"))
    },
    "SELECT CASE WHEN (IFNULL(ac.income, 0) + IFNULL(ac.income2, 0)) <> 9 THEN IFNULL(ac.income, 0) END AS myCol FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (IFNOTNULL(ac.middleName, STR("")))
    },
    "SELECT CASE WHEN ac.middleName IS NOT NULL THEN \"\" ELSE ac.middleName END FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (IFNULL(ac.middleName, STR("")).alias("correctMiddleName"))
    },
    "SELECT IFNULL(ac.middleName, \"\") AS correctMiddleName FROM `Actor` ac",

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

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (SUM(ac.actings) % 100)
    },
    "SELECT (SUM(ac.actings) % 100) FROM `Actor` ac",

    $DSL.select {
        TARGET (Payment.alias("p"))
        FETCH (FLOOR(p.amount).alias("roundDown"), CEIL(p.amount).alias("roundUp"), ABS(p.amount))
    },
    "SELECT FLOOR(p.amount) AS roundDown, CEILING(p.amount) AS roundUp, ABS(p.amount) FROM `Payment` p",

    $DSL.select {
        TARGET (Payment.alias("p"))
        FETCH (ROUND(p.amount, 3).alias("priceOf"))
    },
    "SELECT ROUND(p.amount, 3) AS priceOf FROM `Payment` p",

    $DSL.select {
        TARGET (Payment.alias("p"))
        FETCH (SUM("p.amount + p.id"))
    },
    "SELECT SUM(p.amount + p.id) FROM `Payment` p",

    $DSL.select {
        TARGET (Payment.alias("p"))
        FETCH (ADD($SESSION.listOfInt))
    },
    "SELECT (1 + 2 + 3) FROM `Payment` p",

    $DSL.select {
        TARGET (Payment.alias("p"))
        FETCH (SUBSTRING(p.recieptName, 4, 10), SUBSTRING(p.recieptName, 6), POSITION(p.title, STR("MMM")))
    },
    "SELECT SUBSTRING(p.recieptName, 4, 10), SUBSTRING(p.recieptName, 6), POSITION(\"MMM\" IN p.title) FROM `Payment` p",
]