import java.sql.JDBCType

/**
 * @author IWEERARATHNA
 */
def myQ = $DSL.select {

    TARGET (Rental.alias("r"))

    WHERE {

        BETWEEN (r.customer_id, PARAM("start"), PARAM("end"))

    }

}

$DSL.script {

    def result = RUN(myQ)
    $LOG result

    return result

}