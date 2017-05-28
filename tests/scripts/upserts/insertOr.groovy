
[
    $DSL.insertOrLoad {
        TARGET (Film.alias("f"))
        SET {
            EQ (f.film_id, 1234)
            EQ (f.title, PARAM("title"))
            SET_NULL (f.language_id)
        }
        WHERE {
            GT (f.year, 2010)
        }
    },
    [
            [
                    mysql: "SELECT * FROM `Film` f WHERE f.year > 2010 LIMIT 1"
            ],
            [
                    mysql: ["INSERT INTO `Film` (`film_id`, `title`, `language_id`) VALUES (1234, ?, NULL)", ["title"]]
            ]
    ],
]