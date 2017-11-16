/**
 * @author IWEERARATHNA
 */

$DSL.script {

    $SESSION.manProperty = 1

    if ($DB == 'pg') {
        println('xxx - mysql')
    }

    def innQ = $DSL.select {
        TARGET (Film.alias("f"))

        FETCH (f.film_id, $SESSION.manProperty)

    }

    RUN (innQ)
    RUN ($DSL.select {
        TARGET (Film.alias("f"))

        FETCH (f.film_id, $SESSION.manProperty)

    })
    def result = RUN ("./other_query")
    $LOG result

}