import java.sql.JDBCType

/**
 * @author IWEERARATHNA
 */
def myQ = $DSL.insert {

    TARGET (Song.alias("s"))

    DATA (
        "id": PARAM("id"),
        "name": PARAM("str")
    )

}

$DSL.script {

    def result = RUN("sakila/top_customers")
    $LOG result

    return result

}