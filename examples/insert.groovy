/**
 * @author IWEERARATHNA
 */

// example insert query
$DSL.insert {

    TARGET (Song.alias("s"))

    DATA (
            "id": PARAM("id"),
            "title": PARAM("theTitle")
    )

}