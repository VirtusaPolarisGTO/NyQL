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
        [
            mysql: "INSERT INTO `OtherFilms` (`film_id`, `title`) SELECT * FROM `Film` f"
        ],

        $DSL.insert {
            TARGET (Film.alias("f"))
            INTO (OtherFilms.alias("of"), $IMPORT("inserts/into_imports"))
        },
        [
            mysql: "INSERT INTO `OtherFilms` (`film_id`, `title`) SELECT * FROM `Film` f"
        ],

        $DSL.insert {
            TARGET (Film.alias("f"))
            DATA (
                    "payments": TABLE(payQ),
                    "title": STR("Logan")
            )
        },
        [
            mysql: "INSERT INTO `Film` (`payments`, `title`) VALUES ((SELECT * FROM `Payment` p), \"Logan\")"
        ],

        $DSL.insert {
            TARGET (Film.alias("f"))
            DATA (
                    "payments": TABLE(payQ2),
                    "title": STR("Logan")
            )
        },
        [
            mysql: ["INSERT INTO `Film` (`payments`, `title`) VALUES ((SELECT * FROM `Payment` p WHERE p.payment_id = ?), \"Logan\")",
                    ["payId"]]
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            INTO_TEMP (OtherFilms.alias("of"))
        },
        [
                mysql: "CREATE TEMPORARY TABLE `OtherFilms` AS SELECT * FROM `Film` f",
                mssql: "SELECT * INTO \"OtherFilms\" FROM \"Film\" f",
                pg:    "CREATE TEMPORARY TABLE `OtherFilms` AS SELECT * FROM `Film` f"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (f.id.alias("filmId"), f.title.alias("Title"))
            INTO_TEMP (OtherFilms.alias("of"))
        },
        [
                mysql: "CREATE TEMPORARY TABLE `OtherFilms` AS SELECT f.id AS filmId, f.title AS Title FROM `Film` f",
                mssql: "SELECT f.id AS filmId, f.title AS Title INTO \"OtherFilms\" FROM \"Film\" f",
                pg:    "CREATE TEMPORARY TABLE `OtherFilms` AS SELECT f.id AS filmId, f.title AS Title FROM `Film` f"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (f.id.alias("filmId"), PARAM("userVal").alias("cstVal"))
            INTO_TEMP (OtherFilms.alias("of"))
        },
        [
                mysql: ["CREATE TEMPORARY TABLE `OtherFilms` AS SELECT f.id AS filmId, ? AS cstVal FROM `Film` f", ["userVal"]],
                mssql: ["SELECT f.id AS filmId, ? AS cstVal INTO \"OtherFilms\" FROM \"Film\" f", ["userVal"]],
                pg:    ["CREATE TEMPORARY TABLE `OtherFilms` AS SELECT f.id AS filmId, ? AS cstVal FROM `Film` f", ["userVal"]]
        ]

]