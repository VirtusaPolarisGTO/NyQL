/**
 * @author Isuru Weerarathna
 */
@Field do_cache = true

$DSL.bulkInsert {
    TARGET (Film.alias("f"))

    DATA (
            "id": $SESSION.id,
            "title": PARAM("theTitle"),
    )
}