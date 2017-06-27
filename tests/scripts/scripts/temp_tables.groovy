/**
 * @author iweerarathna
 */
$DSL.script {

    def result = RUN($DSL.select {
        TARGET (Film.alias("f"))
        FETCH (f.id.alias("filmId"), PARAM("userVal").alias("cstVal"))
        INTO_TEMP (OtherFilms.alias("of"))
    })

    // do something with result
    $LOG 'processing result...'
    $LOG result

    // remove temp table
    drop("OtherFilms")

}