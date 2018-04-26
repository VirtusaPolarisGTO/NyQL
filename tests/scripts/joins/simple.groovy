/**
 * @author IWEERARATHNA
 */
[
        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                INNER_JOIN (Film_Actor.alias("fa")) ON (f.film_id, fa.film_id)
                LEFT_JOIN (Actor.alias("a")) ON (fa.actor_id, a.actor_id)
            }
            FETCH ()
        },
        [
            mysql: "SELECT * FROM `Film` f " +
                "INNER JOIN `Film_Actor` fa ON f.film_id = fa.film_id " +
                "LEFT JOIN `Actor` a ON fa.actor_id = a.actor_id"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                JOIN (Film_Actor.alias("fa"))
            }
            FETCH ()
        },
        [
                mysql: "SELECT * FROM `Film` f CROSS JOIN `Film_Actor` fa"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                CROSS_JOIN (Film_Actor.alias("fa"))
            }
            FETCH ()
        },
        [
                mysql: "SELECT * FROM `Film` f CROSS JOIN `Film_Actor` fa"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                FULL_JOIN (Film_Actor.alias("fa")) ON f.film_id, fa.film_id
            }
            FETCH ()
        },
        [
                mysql: "SELECT * FROM `Film` f LEFT JOIN `Film_Actor` fa ON f.film_id = fa.film_id " +
                        "UNION ALL " +
                        "SELECT * FROM `Film` f RIGHT JOIN `Film_Actor` fa ON f.film_id = fa.film_id WHERE f.film_id IS NULL"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                FULL_JOIN (Film_Actor.alias("fa")) ON f.film_id, fa.film_id
            }
            FETCH ()
            WHERE {
                EQ (f.title, PARAM('title'))
            }
        },
        [
                mysql: ["SELECT * FROM `Film` f LEFT JOIN `Film_Actor` fa ON f.film_id = fa.film_id WHERE f.title = ? " +
                        "UNION ALL " +
                        "SELECT * FROM `Film` f RIGHT JOIN `Film_Actor` fa ON f.film_id = fa.film_id WHERE f.film_id IS NULL AND f.title = ?",
                        ["title", "title"]]
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                FULL_JOIN (Film_Actor.alias("fa")) ON {
                    EQ (f.film_id, fa.film_id)
                    AND
                    EQ (f.film_id2, fa.film_id2)
                }
            }
            FETCH ()
            WHERE {
                EQ (f.title, PARAM('title'))
            }
        },
        [
                mysql: ["SELECT * FROM `Film` f LEFT JOIN `Film_Actor` fa ON f.film_id = fa.film_id AND f.film_id2 = fa.film_id2 " +
                                "WHERE f.title = ? " +
                                "UNION ALL " +
                                "SELECT * FROM `Film` f RIGHT JOIN `Film_Actor` fa ON f.film_id = fa.film_id AND f.film_id2 = fa.film_id2 " +
                                "WHERE f.film_id IS NULL AND f.film_id2 IS NULL AND f.title = ?",
                        ["title", "title"]]
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                FULL_JOIN (Film_Actor.alias("fa"))
                FULL_JOIN (Film_Character.alias("fc"))
            }
            FETCH ()
        },
        [
                mysql: "SELECT * FROM `Film` f LEFT JOIN `Film_Actor` fa LEFT JOIN `Film_Character` fc UNION ALL " +
                        "SELECT * FROM `Film` f RIGHT JOIN `Film_Actor` fa LEFT JOIN `Film_Character` fc UNION ALL " +
                        "SELECT * FROM `Film` f RIGHT JOIN `Film_Actor` fa RIGHT JOIN `Film_Character` fc"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                FULL_JOIN (Film_Actor.alias("fa")) ON f.film_id, fa.film_id
                FULL_JOIN (Film_Character.alias("fc")) ON fc.film_id, fa.film_id
            }
            FETCH ()
        },
        [
                mysql: "SELECT * FROM `Film` f LEFT JOIN `Film_Actor` fa ON f.film_id = fa.film_id LEFT JOIN `Film_Character` fc ON fc.film_id = fa.film_id" +
                        " UNION ALL " +
                        "SELECT * FROM `Film` f RIGHT JOIN `Film_Actor` fa ON f.film_id = fa.film_id LEFT JOIN `Film_Character` fc ON fc.film_id = fa.film_id " +
                        "WHERE f.film_id IS NULL" +
                        " UNION ALL " +
                        "SELECT * FROM `Film` f RIGHT JOIN `Film_Actor` fa ON f.film_id = fa.film_id RIGHT JOIN `Film_Character` fc ON fc.film_id = fa.film_id " +
                        "WHERE fa.film_id IS NULL"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                FULL_JOIN (Film_Actor.alias("fa")) ON {
                    EQ (f.film_id, fa.film_id)
                }
                FULL_JOIN (Film_Character.alias("fc")) ON {
                    EQ (fc.film_id, fa.film_id)
                }
            }
            FETCH ()
        },
        [
                mysql: "SELECT * FROM `Film` f LEFT JOIN `Film_Actor` fa ON f.film_id = fa.film_id LEFT JOIN `Film_Character` fc ON fc.film_id = fa.film_id" +
                        " UNION ALL " +
                        "SELECT * FROM `Film` f RIGHT JOIN `Film_Actor` fa ON f.film_id = fa.film_id LEFT JOIN `Film_Character` fc ON fc.film_id = fa.film_id " +
                        "WHERE f.film_id IS NULL" +
                        " UNION ALL " +
                        "SELECT * FROM `Film` f RIGHT JOIN `Film_Actor` fa ON f.film_id = fa.film_id RIGHT JOIN `Film_Character` fc ON fc.film_id = fa.film_id " +
                        "WHERE fa.film_id IS NULL"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                FULL_JOIN (Film_Actor.alias("fa")) ON {
                    EQ (f.film_id, fa.film_id)
                    AND
                    EQ (f.film_id2, fa.film_id2)
                }
                FULL_JOIN (Film_Character.alias("fc")) ON {
                    EQ (fc.film_id, fa.film_id)
                    AND
                    EQ (fc.film_id2, fa.film_id2)
                }
            }
            FETCH ()
        },
        [
                mysql: "SELECT * FROM `Film` f " +
                        "LEFT JOIN `Film_Actor` fa ON f.film_id = fa.film_id AND f.film_id2 = fa.film_id2 " +
                        "LEFT JOIN `Film_Character` fc ON fc.film_id = fa.film_id AND fc.film_id2 = fa.film_id2 " +
                        "UNION ALL " +
                        "SELECT * FROM `Film` f " +
                        "RIGHT JOIN `Film_Actor` fa ON f.film_id = fa.film_id AND f.film_id2 = fa.film_id2 " +
                        "LEFT JOIN `Film_Character` fc ON fc.film_id = fa.film_id AND fc.film_id2 = fa.film_id2 " +
                        "WHERE f.film_id IS NULL AND f.film_id2 IS NULL " +
                        "UNION ALL " +
                        "SELECT * FROM `Film` f " +
                        "RIGHT JOIN `Film_Actor` fa ON f.film_id = fa.film_id AND f.film_id2 = fa.film_id2 " +
                        "RIGHT JOIN `Film_Character` fc ON fc.film_id = fa.film_id AND fc.film_id2 = fa.film_id2 " +
                        "WHERE fa.film_id IS NULL AND fa.film_id2 IS NULL"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                FULL_JOIN (Film_Actor.alias("fa")) ON {
                    EQ (f.film_id, fa.film_id)
                    AND
                    EQ (f.film_id2, fa.film_id2)
                }
                FULL_JOIN (Film_Character.alias("fc")) ON {
                    EQ (fc.film_id, fa.film_id)
                    AND
                    EQ (fc.film_id2, fa.film_id2)
                }
            }
            FETCH ()
            WHERE {
                EQ (f.title, PARAM('title'))
            }
        },
        [
                mysql: ["SELECT * FROM `Film` f " +
                        "LEFT JOIN `Film_Actor` fa ON f.film_id = fa.film_id AND f.film_id2 = fa.film_id2 " +
                        "LEFT JOIN `Film_Character` fc ON fc.film_id = fa.film_id AND fc.film_id2 = fa.film_id2 " +
                        "WHERE f.title = ? " +
                        "UNION ALL " +
                        "SELECT * FROM `Film` f " +
                        "RIGHT JOIN `Film_Actor` fa ON f.film_id = fa.film_id AND f.film_id2 = fa.film_id2 " +
                        "LEFT JOIN `Film_Character` fc ON fc.film_id = fa.film_id AND fc.film_id2 = fa.film_id2 " +
                        "WHERE f.film_id IS NULL AND f.film_id2 IS NULL AND f.title = ? " +
                        "UNION ALL " +
                        "SELECT * FROM `Film` f " +
                        "RIGHT JOIN `Film_Actor` fa ON f.film_id = fa.film_id AND f.film_id2 = fa.film_id2 " +
                        "RIGHT JOIN `Film_Character` fc ON fc.film_id = fa.film_id AND fc.film_id2 = fa.film_id2 " +
                        "WHERE fa.film_id IS NULL AND fa.film_id2 IS NULL AND f.title = ? ",
                        ["title", "title", "title"]]
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN {
                INNER_JOIN (Film_Actor.alias("fa")) ON (f.film_id, fa.film_id)
                LEFT_JOIN (Actor.alias("a")) ON (fa.actor_id, a.actor_id)
            }
            FETCH ()
        },
        [
                mysql: "SELECT * FROM `Film` f " +
                        "INNER JOIN `Film_Actor` fa ON f.film_id = fa.film_id " +
                        "LEFT JOIN `Actor` a ON fa.actor_id = a.actor_id"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                LEFT_OUTER_JOIN (Film_Actor.alias("fa")) ON { EQ(f.film_id, fa.film_id) }
                RIGHT_JOIN (Actor.alias("a")) ON "fa.actor_id = a.actor_id"
            }
            FETCH ()
        },
        [
            mysql: "SELECT * FROM `Film` f " +
                "LEFT JOIN `Film_Actor` fa ON f.film_id = fa.film_id " +
                "RIGHT JOIN `Actor` a ON fa.actor_id = a.actor_id"
        ],

        $DSL.select {
            TARGET (TABLE("Film").alias("f"))
            JOIN (TARGET()) {
                RIGHT_OUTER_JOIN (Film_Actor.alias("fa")) ON {
                    EQ(f.film_id, fa.film_id)
                    AND
                    EQ (f.film_id, fa.second_film_id)
                }
                JOIN (Actor.alias("a")) ON "fa.actor_id = a.actor_id"
            }
            FETCH ()
        },
        [
            mysql: "SELECT * FROM `Film` f " +
                "RIGHT JOIN `Film_Actor` fa ON f.film_id = fa.film_id AND f.film_id = fa.second_film_id " +
                "CROSS JOIN `Actor` a ON fa.actor_id = a.actor_id"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                $IMPORT ("joins/import_join")
            }
            FETCH ()
        },
        [
            mysql: "SELECT * FROM `Film` f INNER JOIN `Actor` ac ON f.actor_id = ac.actor_id"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                $IMPORT ("joins/import_anyjoin")
            }
            FETCH ()
        },
        [
            mysql: "SELECT * FROM `Film` f " +
                "INNER JOIN `Actor` ac ON f.actor_id = ac.actor_id " +
                "RIGHT JOIN `Payment` p ON p.actor_id = ac.actor_id"
        ],
]