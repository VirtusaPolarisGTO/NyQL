/**
 * @author IWEERARATHNA
 */

$DSL.select {

    def gQ = IMPORT("sakila/top_customers")

    TARGET (Rental.alias("rx"))

    JOINING {
        TARGET() INNER_JOIN TABLE(gQ).alias("cu") ON cu.customer_id, rx.customer_id
    }

//    WHERE {
//        EQ (c.customer_id, P("customerId"))
//    }

    ORDER_BY (DESC(cu.customer_id))

}