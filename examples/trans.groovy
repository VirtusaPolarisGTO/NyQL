/**
 * @author IWEERARATHNA
 */
def myQ = $DSL.select {
    TARGET (Rental.alias("r"))
    LIMIT 10
}

$DSL.script {

    RUN("temp_table_test")
    def result = RUN(myQ)
    $LOG result

    return result

}