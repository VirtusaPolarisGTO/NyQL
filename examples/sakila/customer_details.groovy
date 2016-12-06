/**
 * Example NyQL script.
 *
 * @author IWEERARATHNA
 */
$DSL.script {

    // select top customers who has highest number of rentals
    def topCustomers = RUN("sakila/top_customers")

    // for each customer, we send a congratulation message for the lottery,
    // only if they have rentals for the special selected movies...
    for (def customer in topCustomers) {
        def cid = customer["customer_id"]
        $LOG "Loading customer rentals " + cid + "..."

        $SESSION.customerId = cid

        def st = System.currentTimeMillis()
        def rentals = RUN("sakila/special_rentals_of_customer")

        if (rentals.size() > 0) {
            $LOG "  Has " + rentals.size() + "! [Took: " + (System.currentTimeMillis() - st) + " ms]"

            // send an email for lottery
        }

    }

}