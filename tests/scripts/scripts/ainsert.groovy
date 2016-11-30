/**
 * @author Isuru Weerarathna
 */
$DSL.insert {
    TARGET (Film.alias("f"))

    DATA (
            "id": 1,
            "title": PARAM("theTitle"),
    )
}