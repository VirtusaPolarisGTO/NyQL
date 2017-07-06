
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

    $DSL.insertOrLoad {
        TARGET (Film.alias('f'))

        SET {
            EQ (f.title, PARAM('title'))
            EQ (f.year, PARAM('year'))
        }
    },
    [
            [
                    mysql: ["SELECT * FROM `Film` f WHERE f.title = ? AND f.year = ? LIMIT 1",["title", "year"]]
            ],
            [
                    mysql: ["INSERT INTO `Film` (`title`, `year`) VALUES (?, ?)", ["title", "year"]]
            ]
    ],

    $DSL.insertOrLoad {
        TARGET (Film.alias('f'))

        SET {
            EQ (f.title, PARAM('title'))
            EQ (f.director, TABLE(QUERY {
                                TARGET (Person.alias('p'))
                                FETCH (p.person_id)
                                WHERE {
                                    EQ (p.name, PARAM('directorName'))
                                }
                            }))
        }
    },
    [
            [
                    mysql: ["SELECT * FROM `Film` f WHERE f.title = ? " +
                                    "AND f.director IN " +
                                    "(SELECT p.person_id FROM `Person` p WHERE p.name = ?) " +
                                    "LIMIT 1",["title", "directorName"]]
            ],
            [
                    mysql: ["INSERT INTO `Film` (`title`, `director`) VALUES (?, (SELECT p.person_id FROM `Person` p WHERE p.name = ?))", ["title", "directorName"]]
            ]
    ]
]