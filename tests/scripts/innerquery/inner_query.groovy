/**
 * @author IWEERARATHNA
 */
[
        $DSL.select {
            TARGET (Payment.alias("p"))
            FETCH (QUERY {
                TARGET (Film.alias("f"))
                FETCH (COUNT().alias("totalFilms"))
                WHERE {
                    EQ (f.film_id, PARAM("filmId"))
                }
            }, QUERY {
                TARGET (Actor.alias("ac"))
                FETCH ()
            })
        },
        [
            mysql: ["SELECT (SELECT COUNT(*) AS totalFilms FROM `Film` f WHERE f.film_id = ?), " +
                    "(SELECT * FROM `Actor` ac) FROM `Payment` p",
                        ["filmId"]
                    ]
        ],

        $DSL.select {
            TARGET (QUERY {
                        TARGET (TABLE("Film").alias("f"))
                        WHERE {
                            EQ (f.is_active, true)
                        }
                    }.alias("q"))

            JOIN {
                INNER_JOIN (TABLE("Actor").alias("ac")) ON ac.film_id, q.film_id
            }
        },
        [
                mysql: "SELECT * FROM (SELECT * FROM `Film` f WHERE f.is_active = 1) q " +
                        "INNER JOIN `Actor` ac ON ac.film_id = q.film_id"
        ],

        $DSL.select {
            TARGET (QUERY {
                TARGET (TABLE("Film").alias("f"))
                WHERE {
                    EQ (f.is_active, true)
                    AND
                    EQ (f.year, PARAM("year"))
                }
            }.alias("q"))

            JOIN {
                INNER_JOIN (TABLE("Actor").alias("ac")) ON ac.film_id, q.film_id
            }

            WHERE {
                EQ (ac.retired, PARAM("isRetired"))
            }
        },
        [
                mysql: ["SELECT * FROM (SELECT * FROM `Film` f WHERE f.is_active = 1 AND f.year = ?) q " +
                        "INNER JOIN `Actor` ac ON ac.film_id = q.film_id WHERE ac.retired = ?",
                        ["year", "isRetired"]]
        ],

        $DSL.select {
            TARGET (Film.alias("f"))

            JOIN {
                INNER_JOIN (QUERY {
                    TARGET (TABLE("Actor").alias("ac"))
                    WHERE {
                        EQ (ac.retired, true)
                    }
                }.alias("q")) ON q.film_id, f.film_id
            }
        },
        [
                mysql: "SELECT * FROM `Film` f " +
                        "INNER JOIN (SELECT * FROM `Actor` ac WHERE ac.retired = 1) q ON q.film_id = f.film_id"
        ],


        $DSL.select {
            TARGET (Payment.alias("p"))
            WHERE {
                IN (p.payment_id, QUERY {
                    TARGET (Payment.alias("p2"))
                    FETCH (p2.payment_id)
                    WHERE {
                        GT (p2.payment_id, PARAM("thresholdPaymentId"))
                    }
                })
            }
        },
        [
                mysql: ["SELECT * FROM `Payment` p WHERE p.payment_id IN " +
                        "(SELECT p2.payment_id FROM `Payment` p2 WHERE p2.payment_id > ?)",
                            ["thresholdPaymentId"]
                        ]
        ],

        $DSL.insert {
            TARGET (Film.alias("f"))
            DATA (
                    "id": QUERY {
                        TARGET (OtherFilms.alias("otf"))
                        FETCH (otf.film_id)
                        WHERE {
                            LT (otf.film_id, PARAM("minID"))
                        }
                    },

                    "title": PARAM("theTitle")
            )
        },
        [
                mysql: ["INSERT INTO `Film` (`id`, `title`) VALUES " +
                        "((SELECT otf.film_id FROM `OtherFilms` otf WHERE otf.film_id < ?), ?)",
                        ["minID", "theTitle"]]
        ],

        $DSL.update {
            TARGET (Film.alias("f"))
            SET {
                EQ (f.film_id, QUERY {
                        TARGET(OtherFilms.alias("otf"))
                        FETCH(otf.film_id)
                        WHERE {
                            LT(otf.film_id, PARAM("minID"))
                        }
                    })

                EQ (f.title, PARAM("theTitle"))
            }
        },
        [
            mysql: ["UPDATE `Film` f SET f.film_id = SELECT otf.film_id FROM `OtherFilms` otf WHERE otf.film_id < ?, " +
                    "f.title = ?",
                    ["minID", "theTitle"]]
        ]
]