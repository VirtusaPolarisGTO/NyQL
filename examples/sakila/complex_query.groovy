/**
 * @author IWEERARATHNA
 */

$DSL.select {

    // take other query into a variable, and you can use it as a table.
    def gQ = $IMPORT("sakila/top_customers")

    TARGET (Rental.alias("rx"))

    JOIN (TARGET()) {
        // use TABLE(...) to inject inner/sub queries into this query.
        // Or, use inline QUERY { .. } clause.
        INNER_JOIN (TABLE(gQ).alias("cu")) ON cu.customer_id, rx.customer_id

//      INNER_JOIN (TABLE(QUERY {
//           .. type the same query you wrote in sakila/top_customers ..
//           .. ..
//      }).alias("cu")) ON cu.customer_id, rx.customer_id
    }

    ORDER_BY (DESC(cu.customer_id))

}