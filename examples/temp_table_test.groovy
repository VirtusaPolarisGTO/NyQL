/**
 * @author IWEERARATHNA
 */
/**
 * @author IWEERARATHNA
 */

def selIns = $DSL.select {

    TARGET (Film.alias("f"))

    FETCH (f.film_id)

    INTO (TABLE("Isuru"))

}

def selQ = $DSL.select {

    TARGET (TABLE("Isuru").alias("isuru"))

    FETCH ()
}

$DSL.script {

    RUN('ddl/createTemp')
    RUN(selIns)
    def r = RUN(selQ)
    $LOG r
    RUN('ddl/dropTemp')


}
