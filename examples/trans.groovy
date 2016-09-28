import java.sql.JDBCType

/**
 * @author IWEERARATHNA
 */
def myQ = $DSL.insert {

    TARGET (Song.alias("s"))

    DATA (
        "id": P("id", JDBCType.INTEGER),
        "name": P("str", JDBCType.VARCHAR)
    )

}

$DSL.script {

    def result = RUN("update")

    for (int i = 0; i < result.size(); i++) {
        result = RUN "native"
        println(i + " = " + result)
    }

    TRANSACTION {

        // do your thing...

        COMMIT()
    }

    return result
}