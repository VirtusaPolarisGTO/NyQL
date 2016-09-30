/**
 * @author IWEERARATHNA
 */
$DSL.$q {
    EXPECT (TABLE("Film").alias("f"))
    FETCH (f.id, f.year)
}