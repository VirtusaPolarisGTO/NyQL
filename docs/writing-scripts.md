## Scripts
You can write executable scripts using groovy language and also reusing scripts in the query repository. You also can combine java codes as well.

To start a script, you must use `$DSL.script`.

You also can start transactions **(nested transactions are not supported)**, can create temp tables (_within transaction_).

To output something to logs, use `$LOG` function.

Eg:
```groovy

$DSL.script {

    // load top customers which has bought many purchases in last month
    // we receive a list of hashmaps
    def topCustomers = RUN("dashboard/top_customers")

    // for every customer, load all of his/her purchase records
    for (def customer in topCustomers) {
        def cid = customer["customer_id"]
        $LOG "Loading customer rentals " + cid + "..."

        // set the current customer id in the session variables.
        $SESSION["customerId"] = cid

        def rentals = RUN("dashboard/all_rentals_of_customer")
        $LOG "  Customer " + cid + " purchased " + rentals.size() + " items!"
    }

}
```

Eg: Transaction example
```groovy
$DSL.script {
  
    // if transaction failed in the middle, it will automatically rollback.
    TRANSACTION {

        // you may create temp tables within a transaction
        ddl {
            TEMP_TABLE("ATemporaryTable") {
                  // fields/indexes
            }
        }

        // now you can call other query scripts sequentially or construct a custom logic
        RUN "check_pending_inventory"
        RUN "place_orders"
        RUN "update_pending_inventory"
       
        ddl {
             // now you may drop the temp table
             DROP_TEMP_TABLE("ATemporaryTable")
        }

        // last statement inside transaction should be the commit
        COMMIT()
    }

}
```


