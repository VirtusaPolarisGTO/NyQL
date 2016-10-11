/**
 * @author IWEERARATHNA
 */
$DSL.$q {

    EXPECT (Film.alias("f"))

    WHERE {
        EQ (f.rental_duration, PARAM("duration"))
        AND
        GTE (f.replacement_cost, 20.0)
    }

}