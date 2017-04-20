## Scripts
You can write executable scripts using groovy language and also reusing scripts in the query repository. You also can combine java/groovy codes as well.

To start a script, you must use `$DSL.script`.

If you want to execute another script from a script, you can call `RUN ("script-id")`, 
which is `scriptId` is the relative path from script root directory.

__Note:__ Since v1.1.3, you can specify `scriptId` relative to the current script location 
using `@` notation. See below examples.

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

### Relative Script Reference

Since v1.1.3 you can now specify script paths relative to the current running script directory.

There are both pros and cons to this. 
If you have deep nested script hierarchy inside your script root directory, it is easy to specify the relative path.

But in the other hand, someday if you moved the scripts here and there, you have to refactor all the paths in scripts.

Eg:

```groovy

$DSL.script {
    
    // Assume this script is in 'foo/bar' directory.
    
    // calling some script inside foo/bar/baz/inner_script.groovy
    RUN ("foo/bar/baz/inner_script")  // before v1.1.3
    RUN ("@baz/inner_script")
    
    // calling some script in parent directory
    RUN ("foo/foo_script")  // before v1.1.3
    RUN ("@../foo_script")
}
```

