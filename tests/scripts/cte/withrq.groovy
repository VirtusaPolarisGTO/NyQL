/**
 * @author iweerarathna
 */
[
        $DSL.cte {
            WITH_RECURSIVE (Dummy, ['n']) {
                ANCHOR {
                    FETCH (NUM(1))
                }
                RECURSION {
                    TARGET (Dummy)
                    FETCH (Dummy.n + 1)
                    WHERE {
                        LT (Dummy.n, 5)
                    }
                }
            }

            SELECT {
                TARGET (Dummy.alias("dm1"))
            }
        },
        [
                mysql: "WITH RECURSIVE `Dummy` (n) AS " +
                        "(" +
                        "SELECT 1" +
                        " UNION ALL" +
                        " SELECT (n + 1) FROM `Dummy` WHERE n < 5" +
                        ") SELECT * FROM `Dummy` dm1"
        ],

        $DSL.cte {
            WITH_RECURSIVE (Dummy, ['n']) {
                ANCHOR {
                    FETCH (NUM(1))
                }
                RECURSION {
                    TARGET (Dummy)
                    FETCH (Dummy.n + 1)
                    WHERE {
                        LT (Dummy.n, PARAM("limit"))
                    }
                }
            }

            SELECT {
                TARGET (Dummy.alias("dm1"))
                WHERE {
                    LT (dm1.n, PARAM("limit"))
                }
            }
        },
        [
                mysql: ["WITH RECURSIVE `Dummy` (n) AS " +
                        "(" +
                        "SELECT 1" +
                        " UNION ALL" +
                        " SELECT (n + 1) FROM `Dummy` WHERE n < ?" +
                        ") SELECT * FROM `Dummy` dm1 WHERE dm1.n < ?", ["limit", "limit"]]
        ],

        $DSL.cte {
            WITH_RECURSIVE (Dummy, ['n']) {
                ANCHOR {
                    FETCH (NUM(1))
                }
                RECURSION {
                    TARGET (Dummy)
                    FETCH (Dummy.n + 1)
                    WHERE {
                        LT (Dummy.n, PARAM("limit"))
                    }
                }
            }

            SELECT {
                TARGET (Dummy.alias("dm1"))
                JOIN {
                    LEFT_JOIN (Dummy.alias("dm2")) ON dm1.n, dm2.n
                }
                WHERE {
                    LT (dm1.n, PARAM("limit"))
                }
            }
        },
        [
                mysql: ["WITH RECURSIVE `Dummy` (n) AS " +
                                "(" +
                                "SELECT 1" +
                                " UNION ALL" +
                                " SELECT (n + 1) FROM `Dummy` WHERE n < ?" +
                                ") " +
                                "SELECT * FROM `Dummy` dm1 LEFT JOIN `Dummy` dm2 ON dm1.n = dm2.n WHERE dm1.n < ?",
                        ["limit", "limit"]]
        ]
]