/**
 * @author IWEERARATHNA
 */
def innQ = $DSL.select {
    TARGET (Film.alias("f"))
    WHERE {
        EQ (f.film_id, NUM(1))
    }
}

def innQP = $DSL.select {
    TARGET (Film.alias("f"))
    WHERE {
        EQ (f.film_id, PARAM("id"))
    }
}

[
        $DSL.insert {
            TARGET (Film.alias("f"))
            DATA (
                    "film_id": PARAM("id"),
                    "title": PARAM("title")
            )
        },
        ["INSERT INTO `Film` (`film_id`, `title`) VALUES (?, ?)",
         ["id", "title"]],

        $DSL.insert {
            TARGET (Film.alias("f"))
            DATA (
                    "film_id": PARAM("id"),
                    "title": PARAM("title")
            )
            RETURN_KEYS()
        },
        ["INSERT INTO `Film` (`film_id`, `title`) VALUES (?, ?)", ["id", "title"]],

        $DSL.bulkInsert {
            TARGET (Film.alias("f"))
            DATA (
                    "film_id": PARAM("id"),
                    "title": PARAM("title")
            )
        },
        ["INSERT INTO `Film` (`film_id`, `title`) VALUES (?, ?)",
         ["id", "title"]],

        $DSL.insert {
            TARGET (Film.alias("f"))
            DATA (
                    "film_id": TABLE(innQ),
                    "title": PARAM("title")
            )
            RETURN_KEYS()
        },
        ["INSERT INTO `Film` (`film_id`, `title`) VALUES ((SELECT * FROM `Film` f WHERE f.film_id = 1), ?)", ["title"]],

        $DSL.insert {
            TARGET (Film.alias("f"))
            DATA (
                    "film_id": TABLE(innQP),
                    "title": PARAM("title")
            )
        },
        ["INSERT INTO `Film` (`film_id`, `title`) VALUES ((SELECT * FROM `Film` f WHERE f.film_id = ?), ?)", ["id", "title"]],
]