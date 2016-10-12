/**
 * @author IWEERARATHNA
 */
@Field do_cache = true

$DSL.select {
    TARGET (Film.alias("f"))
    FETCH ()
}