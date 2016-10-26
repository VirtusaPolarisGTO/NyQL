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
        ["SELECT (SELECT COUNT(*) AS totalFilms FROM `Film` f WHERE f.film_id = ?), " +
                 "(SELECT * FROM `Actor` ac) FROM `Payment` p",
         ["filmId"]],


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
        ["SELECT * FROM `Payment` p WHERE p.payment_id IN " +
                "(SELECT p2.payment_id FROM `Payment` p2 WHERE p2.payment_id > ?)",
                ["thresholdPaymentId"]],

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
        ["INSERT INTO `Film` (`id`, `title`) VALUES " +
                 "((SELECT otf.film_id FROM `OtherFilms` otf WHERE otf.film_id < ?), ?)",
            ["minID", "theTitle"]]
]