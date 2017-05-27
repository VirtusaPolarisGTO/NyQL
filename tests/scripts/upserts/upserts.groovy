
[
        $DSL.upsert {
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
            ],
            [
                mysql: ["UPDATE `Film` f SET f.film_id = 1234, f.title = ?, f.language_id = NULL WHERE f.year > 2010", ["title"]]
            ]
        ],

        $DSL.upsert {
            TARGET (Film.alias("f"))
            SET {
                EQ (f.film_id, 1234)
                EQ (f.title, PARAM("title"))
                SET_NULL (f.language_id)
            }
            WHERE {
                GT (f.year, 2010)
            }
            RETURN_AFTER()      // should generate an additional query to get updated result
        },
        [
                [
                        mysql: "SELECT * FROM `Film` f WHERE f.year > 2010 LIMIT 1"
                ],
                [
                        mysql: ["INSERT INTO `Film` (`film_id`, `title`, `language_id`) VALUES (1234, ?, NULL)", ["title"]]
                ],
                [
                        mysql: ["UPDATE `Film` f SET f.film_id = 1234, f.title = ?, f.language_id = NULL WHERE f.year > 2010", ["title"]]
                ],
                [
                        mysql: "SELECT * FROM `Film` f WHERE f.year > 2010 LIMIT 1"
                ]
        ],
        $DSL.upsert {
            TARGET (Film.alias("f"))
            SET {
                EQ (f.film_id, 1234)
                EQ (f.title, PARAM("title"))
                SET_NULL (f.language_id)
            }
            WHERE {
                GT (f.year, 2010)
            }
            RETURN_BEFORE()         // should not generate additional query
        },
        [
                [
                        mysql: "SELECT * FROM `Film` f WHERE f.year > 2010 LIMIT 1"
                ],
                [
                        mysql: ["INSERT INTO `Film` (`film_id`, `title`, `language_id`) VALUES (1234, ?, NULL)", ["title"]]
                ],
                [
                        mysql: ["UPDATE `Film` f SET f.film_id = 1234, f.title = ?, f.language_id = NULL WHERE f.year > 2010", ["title"]]
                ]
        ],

        $DSL.upsert {
            TARGET (Film.alias("f"))
            SET {
                EQ (f.film_id, 1234)
                EQ (f.title, PARAM("title"))
                SET_NULL (f.language_id)
            }
            WHERE {
                GT (f.year, 2010)
            }
            RETURN_COLUMNS (f.film_id, f.language_id.alias('langId')) // should generate an additional query to get updated result
        },
        [
                [
                        mysql: "SELECT * FROM `Film` f WHERE f.year > 2010 LIMIT 1"
                ],
                [
                        mysql: ["INSERT INTO `Film` (`film_id`, `title`, `language_id`) VALUES (1234, ?, NULL)", ["title"]]
                ],
                [
                        mysql: ["UPDATE `Film` f SET f.film_id = 1234, f.title = ?, f.language_id = NULL WHERE f.year > 2010", ["title"]]
                ],
                [
                        mysql: "SELECT f.film_id, f.language_id AS langId FROM `Film` f WHERE f.year > 2010 LIMIT 1"
                ]
        ],
        $DSL.upsert {
            TARGET (Film.alias("f"))
            SET {
                EQ (f.film_id, 1234)
                EQ (f.title, PARAM("title"))
                SET_NULL (f.language_id)
            }
            WHERE {
                GT (f.year, 2010)
            }
            RETURN_COLUMNS (f.film_id, f.language_id.alias('langId')) // ignoring generation of an additional query
            RETURN_NONE()
        },
        [
                [
                        mysql: "SELECT * FROM `Film` f WHERE f.year > 2010 LIMIT 1"
                ],
                [
                        mysql: ["INSERT INTO `Film` (`film_id`, `title`, `language_id`) VALUES (1234, ?, NULL)", ["title"]]
                ],
                [
                        mysql: ["UPDATE `Film` f SET f.film_id = 1234, f.title = ?, f.language_id = NULL WHERE f.year > 2010", ["title"]]
                ]
        ],

]