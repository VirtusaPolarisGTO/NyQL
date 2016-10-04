/**
 * @author IWEERARATHNA
 */
$DSL.$q {

    EXPECT (Film.alias("f"))

    SET {
        EQ (f.title, PARAM("title"))
        EQ (f.language_id, 1)
    }

}