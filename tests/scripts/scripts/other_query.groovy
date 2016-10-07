/**
 * @author IWEERARATHNA
 */
def innQ = $DSL.select {
    TARGET (Rental.alias("r"))
    JOIN (TARGET()) {
        INNER_JOIN (Customer.alias("c")) ON c.customer_id, r.customer_id
    }
    FETCH (r.rental_id, r.inventory_id)
}

$DSL.select {
    TARGET (TABLE(innQ).alias("oth"))
    FETCH (oth.rental_id)
    LIMIT 10
}