/**
 * @author IWEERARATHNA
 */
[
        $DSL.update {
            TARGET (Film.alias("f"))
            SET {
                EQ (f.film_id, 1234)
                EQ (f.title, PARAM("title"))
                SET_NULL (f.language_id)
            }
        },
        "UPDATE `Film` f SET f.film_id = 1234, f.title = ?, f.language_id = NULL",

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
        "UPDATE `Film` f SET f.film_id = 1234, f.title = ?, f.language_id = NULL WHERE f.year = 2016",

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
        "UPDATE `Film` f LEFT JOIN `Actor` ac ON f.actor_id = ac.actor_id SET f.film_id = 1234, f.title = ? WHERE f.year = 2016",

        $DSL.update {
            TARGET (Film.alias("f"))
            SET {
                EQ (f.film_id, 1234)
                $IMPORT "updates/import_part"
            }
        },
        "UPDATE `Film` f SET f.film_id = 1234, f.title = ?, f.language_id = 1",

]