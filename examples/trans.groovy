/**
 * @author IWEERARATHNA
 */
def myQ = $DSL.select {

    TARGET (Rental.alias("r"))

    FETCH (PARAM("cost").alias("cost"), r.rental_id)

    WHERE {

        EQ (r.customer_id, PARAM("amap.cids.cid"))

    }

}

$DSL.script {

    def result = RUN(myQ)
    $LOG result

    return result

}