/**
 * @author IWEERARATHNA
 */
[
        $DSL.delete {
            TARGET (Film)
            JOIN (TARGET()) {
                INNER_JOIN (Actor) ON Actor.id, Film.id
            }
            WHERE {
                EQ (Film.film_id, 1234)
            }
            ON_UNIQUE_KEYS(Film.film_id)
        },
        [
            mysql:  "DELETE `Film` FROM `Film` INNER JOIN `Actor` ON `Actor`.id = `Film`.id WHERE `Film`.film_id = 1234",
            h2:  "DELETE FROM \"Film\" WHERE film_id IN (SELECT Film_tmpny.film_id FROM \"Film\" Film_tempny INNER JOIN \"Actor\" ON \"Actor\".id = Film_tempny.id WHERE Film_tempny.film_id = 1234)",
            pg:     ""
        ],

        $DSL.delete {
            TARGET (Film)
            JOIN {
                INNER_JOIN (Actor) ON Actor.id, Film.id
            }
            WHERE {
                EQ (Film.film_id, 1234)
            }
            ON_UNIQUE_KEYS(Film.film_id)
        },
        [
                mysql:  "DELETE `Film` FROM `Film` INNER JOIN `Actor` ON `Actor`.id = `Film`.id WHERE `Film`.film_id = 1234",
                h2:  "DELETE FROM \"Film\" WHERE film_id IN (SELECT Film_tmpny.film_id FROM \"Film\" Film_tempny INNER JOIN \"Actor\" ON \"Actor\".id = Film_tempny.id WHERE Film_tempny.film_id = 1234)",
                pg:     ""
        ],

        $DSL.delete {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                INNER_JOIN (Actor.alias("ac")) ON ac.id, f.id
            }
            WHERE {
                EQ (f.film_id, 1234)
            }
            ON_UNIQUE_KEYS(f.film_id)
        },
        [
            mysql:  "DELETE f FROM `Film` f INNER JOIN `Actor` ac ON ac.id = f.id WHERE f.film_id = 1234",
            h2:  "DELETE FROM `Film` WHERE film_id IN (SELECT f.film_id FROM `Film` f INNER JOIN `Actor` ac ON ac.id = f.id WHERE f.film_id = 1234)",
            pg:     ""
        ],

        $DSL.delete {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                INNER_JOIN (Actor.alias("ac")) ON ac.id, f.id
            }
            WHERE {
                EQ (f.film_id, 1234)
            }
            ON_UNIQUE_KEYS(f.film_id, f.title)
        },
        [
                mysql:  "DELETE f FROM `Film` f INNER JOIN `Actor` ac ON ac.id = f.id WHERE f.film_id = 1234",
                h2:  "DELETE FROM `Film` WHERE (film_id, title) IN (SELECT (f.film_id, f.title) FROM `Film` f INNER JOIN `Actor` ac ON ac.id = f.id WHERE f.film_id = 1234)",
                pg:     ""
        ]

]