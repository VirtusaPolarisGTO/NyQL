/**
 * Select customers who took most rentals more than
 * parameter indicated by 'minRentals' limiting to top 5 records.
 *
 * @author IWEERARATHNA
 */
$DSL.select {

    TARGET (Rental.alias("r"))

    FETCH (r.customer_id, COUNT().alias("totalRentals"))

    GROUP_BY (r.customer_id)
    HAVING {
        GTE (totalRentals, PARAM("minRentals"))
    }

    ORDER_BY (DESC(totalRentals))

    LIMIT 5
}
