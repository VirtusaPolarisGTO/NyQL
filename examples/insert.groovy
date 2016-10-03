/**
 * @author IWEERARATHNA
 */

$DSL.insert {

    TARGET (Song.alias("s"))

    DATA (
            "id": PARAM("id"),
            "name": PARAM("str")
    )

}