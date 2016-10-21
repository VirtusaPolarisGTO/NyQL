/**
 * @author IWEERARATHNA
 */
def payQ = $DSL.select {
    TARGET (Payment.alias("p"))
}

def payQ2 = $DSL.select {
    TARGET (Payment.alias("p"))
    WHERE {
        EQ (p.payment_id, PARAM("payId"))
    }
}

[
        $DSL.select {
            TARGET (Film.alias("f"))
            INTO (OtherFilms.alias("of"), $IMPORT("inserts/into_imports"))
        },
        "INSERT INTO `OtherFilms` (`film_id`, `title`) SELECT * FROM `Film` f",

        $DSL.insert {
            TARGET (Film.alias("f"))
            INTO (OtherFilms.alias("of"), $IMPORT("inserts/into_imports"))
        },
        "INSERT INTO `OtherFilms` (`film_id`, `title`) SELECT * FROM `Film` f",

        $DSL.insert {
            TARGET (Film.alias("f"))
            DATA (
                    "payments": TABLE(payQ),
                    "title": STR("Logan")
            )
        },
        "INSERT INTO `Film` (`payments`, `title`) VALUES ((SELECT * FROM `Payment` p), \"Logan\")",

        $DSL.insert {
            TARGET (Film.alias("f"))
            DATA (
                    "payments": TABLE(payQ2),
                    "title": STR("Logan")
            )
        },
        ["INSERT INTO `Film` (`payments`, `title`) VALUES ((SELECT * FROM `Payment` p WHERE p.payment_id = ?), \"Logan\")",
                ["payId"]]

]