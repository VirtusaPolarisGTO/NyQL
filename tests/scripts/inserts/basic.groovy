/**
 * @author IWEERARATHNA
 */
[
        $DSL.insert {
            TARGET (Film.alias("f"))
            DATA (
                    "film_id": PARAM("id"),
                    "title": PARAM("title")
            )
        },
        "INSERT INTO `Film` (`film_id`, `title`) VALUES (?, ?)",

        $DSL.insert {
            TARGET (Film.alias("f"))
            DATA (
                    "film_id": PARAM("id"),
                    "title": PARAM("title")
            )
            RETURN_KEYS()
        },
        "INSERT INTO `Film` (`film_id`, `title`) VALUES (?, ?)"

]