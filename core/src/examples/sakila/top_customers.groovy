/**
 * @author IWEERARATHNA
 */
$DSL.select {

    TARGET (Rental.alias("r"))

    FETCH (r.customer_id,
            COUNT().alias("totalRentals"))

    WHERE {
        EQ (r.customer_id, 1234)
        AND
        NOTLIKE (r.name, STR("hello world!"))
    }

    GROUP_BY (r.customer_id)
    HAVING {
        GTE (COUNT(), PARAM("minRentals"))
    }

    ORDER_BY (DESC(totalRentals))

    LIMIT 5
}