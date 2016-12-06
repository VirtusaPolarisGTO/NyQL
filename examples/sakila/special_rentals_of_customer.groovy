/**
 * @author IWEERARATHNA
 */
$DSL.select {

    TARGET (Rental.alias("r"))

    JOIN (TARGET()) {
        INNER_JOIN (Inventory.alias("iv")) ON r.inventory_id, iv.inventory_id
        INNER_JOIN (Customer.alias("c")) ON r.customer_id, c.customer_id
        INNER_JOIN (Film.alias("f")) ON iv.film_id, f.film_id
    }

    FETCH ()

    WHERE {
        EQ (c.customer_id, PARAM("customerId"))
        IN (f.film_id, PARAMLIST("specialMovieIds"))
    }

}