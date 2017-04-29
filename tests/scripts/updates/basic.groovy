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
        $DSL.update {
            TARGET (Film.alias("f"))
            SET {
                EQ (f.film_id, 1234)
                EQ (f.title, PARAM("title"))
                SET_NULL (f.language_id)
            }
        },
        [
            mysql: ["UPDATE `Film` f SET f.film_id = 1234, f.title = ?, f.language_id = NULL", ["title"]],
            mssql: ['UPDATE "Film" f SET f.film_id = 1234, f.title = ?, f.language_id = NULL', ["title"]]
        ],

        $DSL.update {
            TARGET (Film.alias("f"))
            SET {
                EQ (f.film_id, 1234)
                EQ (f.title, PARAM("title"))
                if ($SESSION.alwaysTrue) {
                    SET_NULL(f.language_id)
                }
            }
            WHERE {
                EQ (f.year, 2016)
            }
        },
        [
            mysql: ["UPDATE `Film` f SET f.film_id = 1234, f.title = ?, f.language_id = NULL WHERE f.year = 2016", ["title"]],
            mssql: ['UPDATE "Film" f SET f.film_id = 1234, f.title = ?, f.language_id = NULL WHERE f.year = 2016', ["title"]]
        ],

        $DSL.update {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                LEFT_JOIN (Actor.alias("ac")) ON f.actor_id, ac.actor_id
            }
            SET {
                EQ (f.film_id, 1234)
                EQ (f.title, PARAM("title"))
                if ($SESSION.thisValueNotExist) {
                    SET_NULL(f.language_id)
                }
            }
            WHERE {
                EQ (f.year, 2016)
            }
        },
        [
            mysql: ["UPDATE `Film` f LEFT JOIN `Actor` ac ON f.actor_id = ac.actor_id SET f.film_id = 1234, f.title = ? " +
                    "WHERE f.year = 2016",
                    ["title"]],
            mssql: ['UPDATE f SET f.film_id = 1234, f.title = ? FROM "Film" f LEFT JOIN "Actor" ac ON f.actor_id = ac.actor_id ' +
                            'WHERE f.year = 2016',
                    ["title"]]
        ],

        $DSL.update {
            TARGET (Film.alias("f"))
            JOIN {
                LEFT_JOIN (Actor.alias("ac")) ON f.actor_id, ac.actor_id
            }
            SET {
                EQ (f.film_id, 1234)
                EQ (f.title, PARAM("title"))
                if ($SESSION.thisValueNotExist) {
                    SET_NULL(f.language_id)
                }
            }
            WHERE {
                EQ (f.year, 2016)
            }
        },
        [
                mysql: ["UPDATE `Film` f LEFT JOIN `Actor` ac ON f.actor_id = ac.actor_id SET f.film_id = 1234, f.title = ? " +
                                "WHERE f.year = 2016",
                        ["title"]],
                mssql: ['UPDATE f SET f.film_id = 1234, f.title = ? FROM "Film" f LEFT JOIN "Actor" ac ON f.actor_id = ac.actor_id ' +
                                'WHERE f.year = 2016',
                        ["title"]]
        ],

        $DSL.update {
            TARGET (Film.alias("f"))
            SET {
                EQ (f.film_id, 1234)
                $IMPORT "updates/import_part"
            }
        },
        [
            mysql: ["UPDATE `Film` f SET f.film_id = 1234, f.title = ?, f.language_id = 1", ["title"]],
            mssql: ['UPDATE "Film" f SET f.film_id = 1234, f.title = ?, f.language_id = 1', ["title"]]
        ],

        $DSL.update {
            TARGET (Film.alias("f"))
            SET {
                EQ (f.film_id, TABLE(innQ))
            }
        },
        [
            mysql: "UPDATE `Film` f SET f.film_id = (SELECT * FROM `Film` f WHERE f.film_id = 1)",
            mssql: 'UPDATE "Film" f SET f.film_id = (SELECT * FROM "Film" f WHERE f.film_id = 1)'
        ],

        $DSL.update {
            TARGET (Film.alias("f"))
            SET {
                EQ (f.film_id, TABLE(innQP))
            }
        },
        [
            mysql: ["UPDATE `Film` f SET f.film_id = (SELECT * FROM `Film` f WHERE f.film_id = ?)", ["id"]],
            mssql: ['UPDATE "Film" f SET f.film_id = (SELECT * FROM "Film" f WHERE f.film_id = ?)', ["id"]]
        ],

        $DSL.update {
            TARGET (Film.alias("f"))
            SET {
                EQ (f.film_id, 1234)
                EQ (f.releaseDate, PARAM_DATE("relDate"))
                EQ (f.debutTime, PARAM_TIMESTAMP("debutTime"))
                EQ (f.debutTime2, PARAM_TIMESTAMP("debutTime2", "YYYY-MM-DDThh:mm:ssTZD"))
                SET_NULL (f.language_id)
            }
        },
        [
            mysql: ["UPDATE `Film` f SET f.film_id = 1234, f.releaseDate = ?, f.debutTime = ?, f.debutTime2 = ?, f.language_id = NULL",
                    ["relDate", "debutTime", "debutTime2"]],
            mssql: ['UPDATE "Film" f SET f.film_id = 1234, f.releaseDate = ?, f.debutTime = ?, f.debutTime2 = ?, f.language_id = NULL',
                    ["relDate", "debutTime", "debutTime2"]]
        ],

        $DSL.update {
            TARGET (Film.alias("f"))
            SET {
                EQ (f.film_id, 1234)
                $IMPORT_UNSAFE ("updates/non-existing-script")
            }
        },
        [
            mysql: "UPDATE `Film` f SET f.film_id = 1234",
            mssql: 'UPDATE "Film" f SET f.film_id = 1234'
        ],
]