import com.virtusa.gto.nyql.utils.QueryType

/**
 * @author Isuru Weerarathna
 */
[
    $DSL.nativeQuery (
        QueryType.SELECT,
        [
            mysql: [[],
                    "SELECT * FROM `Film` f WHERE f.film_id > 100 LIMIT 5"
                    ]
        ]
    ),
    [
        mysql: "SELECT * FROM `Film` f WHERE f.film_id > 100 LIMIT 5"
    ],

    $DSL.nativeQuery (
            QueryType.SELECT,
            [
                    mysql: [[$DSL.PARAM("filmId")],
                            "SELECT * FROM `Film` f WHERE f.film_id > ? LIMIT 5"
                    ]
            ]
    ),
    [
            mysql: ["SELECT * FROM `Film` f WHERE f.film_id > ? LIMIT 5", ["filmId"]]
    ],

    $DSL.nativeQuery (
            QueryType.SELECT,
            [
                    mysql: [[$DSL.PARAMLIST("filmIds"), $DSL.PARAM("recordLimit")],
                            "SELECT * FROM `Film` f WHERE f.film_id IN ::filmIds:: LIMIT ?"
                    ]
            ]
    ),
    [
            mysql: ["SELECT * FROM `Film` f WHERE f.film_id IN ::filmIds:: LIMIT ?", ["filmIds", "recordLimit"]]
    ],
]