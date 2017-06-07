/**
 * @author iweerarathna
 */
def q = $DSL.insertOrLoad {
    TARGET (Film.alias("f"))
    SET {
        EQ (f.film_id, 1234)
        EQ (f.title, PARAM("title"))
        SET_NULL (f.language_id)
    }
    WHERE {
        GT (f.year, 2010)
    }
}

$DSL.script {

    RUN (q)

}