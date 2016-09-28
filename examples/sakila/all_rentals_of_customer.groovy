import java.sql.JDBCType

/**
 * @author IWEERARATHNA
 */
$DSL.select {

    TARGET (Rental.alias("r"))

    JOINING {
        TARGET() \
            INNER_JOIN (Inventory.alias("iv")) ON r.inventory_id, iv.inventory_id \
            INNER_JOIN (Customer.alias("c")) ON r.customer_id, c.customer_id \
            INNER_JOIN (Film.alias("f")) ON iv.film_id, f.film_id
    }

    FETCH (r.rental_date, EPOCH_TIMESTAMP(r.rental_date),
            CAST(r.rental_date, "DATE"),
            EPOCH_TIMESTAMP(CAST(r.rental_date, "DATE")),
            FROM_EPOCH(EPOCH_TIMESTAMP(CAST(r.rental_date, "DATE"))))

    WHERE {
        EQ (c.customer_id, P("customerId"))
    }

    ORDER_BY (DESC(r.rental_date))
}