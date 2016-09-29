/**
 * @author IWEERARATHNA
 */
import java.sql.JDBCType

/**
 * @author IWEERARATHNA
 */
def tempCreate = $DSL.ddl {

    TEMP_TABLE ("Isuru") {
        FIELD ("id", DFieldType.INT)
    }

}

def tempDrop = $DSL.ddl {
    DROP_TEMP_TABLE ("Isuru")
}

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

    RUN(tempCreate)
    RUN(selIns)
    def r = RUN(selQ)
    $LOG r
    RUN(tempDrop)


}
