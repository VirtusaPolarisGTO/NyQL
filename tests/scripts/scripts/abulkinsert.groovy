/**
 * @author Isuru Weerarathna
 */
$DSL.bulkInsert {
    TARGET (Film.alias("f"))

    DATA (
            "id": 1,
            "title": PARAM("theTitle"),
    )
}