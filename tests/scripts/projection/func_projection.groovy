
/**
 * @author IWEERARATHNA
 */
def innQ = $DSL.select {
    TARGET (Film.alias("f"))
}

def innQP = $DSL.select {
    TARGET (Film.alias("f"))
    WHERE {
        EQ (f.film_id, PARAM("filmId"))
    }
}

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
        FETCH (IFNOTNULL(ac.middleName, STR("")))
    },
    "SELECT CASE WHEN ac.middleName IS NOT NULL THEN \"\" ELSE ac.middleName END FROM `Actor` ac",

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
        TARGET (Actor.alias("ac"))
        FETCH (SUM(DISTINCT(ac.actings)))
    },
    "SELECT SUM(DISTINCT(ac.actings)) FROM `Actor` ac",

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

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH (f.rental_duration & 5, f.length | f.rental_duration, f.length ^ 10, BITNOT(f.rental_duration))
    },
    "SELECT f.rental_duration & 5, f.length | f.rental_duration, f.length ^ 10, ~f.rental_duration FROM `Film` f",

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH (BITAND(f.rental_duration, 5), BITOR(f.length, f.rental_duration), BITXOR(f.length, 10))
    },
    "SELECT f.rental_duration & 5, f.length | f.rental_duration, f.length ^ 10 FROM `Film` f",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (CASE {
            WHEN { EXISTS (innQ) } THEN { STR("replaced") }
            ELSE { STR("deleted") }
        }
        )
    },
    "SELECT CASE WHEN EXISTS (SELECT * FROM `Film` f) THEN \"replaced\" ELSE \"deleted\" END FROM `Actor` ac",

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (EXISTS(innQ))
    },
    "SELECT EXISTS(SELECT * FROM `Film` f) FROM `Actor` ac",

    $DSL.select {
        FETCH (EXISTS(innQ))
    },
    "SELECT EXISTS(SELECT * FROM `Film` f)",

    $DSL.select {
        FETCH (EXISTS(innQP))
    },
    ["SELECT EXISTS(SELECT * FROM `Film` f WHERE f.film_id = ?)", ["filmId"]],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (CAST_INT(ac.age), CAST_STR(ac.birthYear).alias("bYear"))
    },
    "SELECT CAST(ac.age AS SIGNED), CAST(ac.birthYear AS CHAR) AS bYear FROM `Actor` ac",

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH (STR_REPLACE(f.title, STR("w"), STR("www")),
                STR_REPLACE(f.description, f.title, STR("movie")),
                STR_REPLACE(f.description, STR("pholder"), f.place))
    },
    "SELECT REPLACE(f.title, \"w\", \"www\"), REPLACE(f.description, f.title, \"movie\"), " +
            "REPLACE(f.description, \"pholder\", f.place) FROM `Film` f",

    $DSL.select {
        TARGET (Film.alias("f"))
        FETCH (DATE_DIFF_SECONDS(EPOCH_TO_DATETIME(NUM(0)), f.release_date).alias("releaseDate"))
    },
    "SELECT TIMESTAMPDIFF(SECOND, FROM_UNIXTIME(0 / 1000), f.release_date) AS releaseDate FROM `Film` f"
]