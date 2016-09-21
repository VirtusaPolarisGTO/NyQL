import java.sql.JDBCType

/**
 * @author IWEERARATHNA
 */
$DSL.select {

    TARGET (Rental.alias("r"))

    FETCH (r.customer_id,
            COUNT().alias("totalRentals"))

    GROUP_BY (r.customer_id)
    HAVING {
        ON (COUNT(), ">=", P("minRentals", JDBCType.INTEGER))
    }

    ORDER_BY (DESC(totalRentals))

    LIMIT 5
}