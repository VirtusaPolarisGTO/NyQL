import java.sql.JDBCType

/**
 * @author IWEERARATHNA
 */
def myQ = $DSL.select {

    TARGET (Rental.alias("r"))

    FETCH (PARAM("cost").alias("cost"), PARAM("filmId").alias("films"), r.rental_id)

    WHERE {

        LIKE (r.customer_id, PARAM("start"))

    }

}

$DSL.script {

    def result = RUN(myQ)
    $LOG result

    return result

}