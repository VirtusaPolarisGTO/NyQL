/**
 * @author IWEERARATHNA
 */

def s = $SESSION

$DSL.script {

    def topCustomers = RUN("top_customers")

    for (def customer in topCustomers) {
        def cid = customer["customer_id"]
        $LOG "Loading customer rentals " + cid + "..."

        s["customerId"] = cid

        def st = System.currentTimeMillis()
        def rentals = RUN("all_rentals_of_customer")
        $LOG "  Has " + rentals.size() + "! [Took: " + (System.currentTimeMillis() - st) + " ms]"
    }

}