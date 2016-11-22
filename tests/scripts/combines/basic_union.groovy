/**
 * @author IWEERARATHNA
 */
def q1 = $DSL.select {
    TARGET (Film.alias("f"))
    FETCH (f.film_id, f.title)
}

def q2 = $DSL.select {
    TARGET (ForeignFilms.alias("ff"))
    FETCH (ff.film_id, ff.title)
}

def result_left = $DSL.select {
    TARGET (TABLE(q1).alias("q11"))
    JOIN (TARGET()) {
        LEFT_JOIN (TABLE("org_unit_role_query").alias("q22")) ON (q11.user_id, q22.userId)
    }
    FETCH (ALL, IFNULL(q11.user_id,q22.userId).alias("user_id"))
}

def result_right = $DSL.select {
    TARGET (TABLE(q1).alias("q11"))
    JOIN (TARGET()) {
        RIGHT_JOIN (TABLE("org_unit_role_query").alias("q22")) ON (q11.user_id, q22.userId)
    }
    FETCH (ALL, IFNULL(q11.user_id,q22.userId).alias("user_id"))
    WHERE{
        ISNULL(q11.user_id)
    }
}

def aUnion = $DSL.union(q1, q2)

[
    $DSL.union (q1, q2),
        [
            mysql: "(SELECT f.film_id, f.title FROM `Film` f) UNION ALL (SELECT ff.film_id, ff.title FROM `ForeignFilms` ff)"
        ],

    $DSL.unionDistinct (q1, q2),
        [
            mysql: "(SELECT f.film_id, f.title FROM `Film` f) UNION (SELECT ff.film_id, ff.title FROM `ForeignFilms` ff)"
        ],

    $DSL.union(result_left, result_right),
        [
            mysql:  "(SELECT *, IFNULL(q11.user_id, q22.userId) AS user_id " +
                    "FROM (SELECT f.film_id, f.title FROM `Film` f) q11 " +
                    "LEFT JOIN `org_unit_role_query` q22 ON q11.user_id = q22.userId) " +
                    "UNION ALL " +
                    "(SELECT *, IFNULL(q11.user_id, q22.userId) AS user_id " +
                    "FROM (SELECT f.film_id, f.title FROM `Film` f) q11 " +
                    "RIGHT JOIN `org_unit_role_query` q22 ON q11.user_id = q22.userId WHERE q11.user_id IS NULL)"
        ],

    $DSL.delete {
        TARGET(Payment.alias("p"))
        JOIN (TARGET()) {
            INNER_JOIN (TABLE(aUnion).alias("t")) ON p.id, t.id
        }
    },
        [
            mysql:  "DELETE p FROM `Payment` p " +
                    "INNER JOIN ((SELECT f.film_id, f.title FROM `Film` f) " +
                    "UNION ALL (SELECT ff.film_id, ff.title FROM `ForeignFilms` ff)) t ON p.id = t.id"
        ]
]