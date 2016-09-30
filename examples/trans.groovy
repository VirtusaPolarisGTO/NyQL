import java.sql.JDBCType

/**
 * @author IWEERARATHNA
 */
def myQ = $DSL.select {

    TARGET (Rental.alias("r"))

    WHERE {

        LIKE (r.customer_id, PARAM("start"))

    }

}

$DSL.script {

    def result = RUN(myQ)
    $LOG result

    return result

}