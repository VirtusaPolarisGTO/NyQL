import com.virtusa.gto.nyql.utils.QueryType

/**
 * @author Isuru Weerarathna
 */
[
        $DSL.nativeQuery (
                QueryType.SELECT,
                [],
                "SELECT * FROM `Film` f WHERE f.film_id > 100 LIMIT 5"
        ),
        [
                mysql: "SELECT * FROM `Film` f WHERE f.film_id > 100 LIMIT 5"
        ],

        $DSL.nativeQuery (
                QueryType.SELECT,
                [$DSL.PARAM("filmId"), $DSL.PARAM("rLimit")],
                "SELECT * FROM `Film` f WHERE f.film_id > ? LIMIT ?"
        ),
        [
                mysql: ["SELECT * FROM `Film` f WHERE f.film_id > ? LIMIT ?", ["filmId", "rLimit"]]
        ],

        $DSL.nativeQuery (
                QueryType.SELECT,
                [$DSL.PARAMLIST("filmId")],
                "SELECT * FROM `Film` f WHERE f.film_id IN ::filmId:: LIMIT 5"
        ),
        [
                mysql: ["SELECT * FROM `Film` f WHERE f.film_id IN ::filmId:: LIMIT 5", ["filmId"]]
        ]
]