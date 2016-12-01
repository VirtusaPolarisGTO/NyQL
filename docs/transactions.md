## Transactions
Transactions can only be defined inside scripts, i.e. `$DSL.script { ... }`.

**Note: In case of a transaction failure, it will automatically be rollbacked.**

### Transaction Commands:
* TRANSACTION
* COMMIT
* CHECKPOINT
* ROLLBACK

#### To start a transaction
```groovy
$DSL.script {
    
    TRANSACTION {
        // do stuff here...

        // must call COMMIT() manually at the end
        COMMIT()
    }

    // starting a transaction automatically calls COMMIT() at the end if transaction is successful.
    TRANSACTION {
        // set auto commit
        AUTO_COMMIT()

        // do stuff here...

    }
}
```

#### Working with checkpoints
In case of failure, you can move back to a previous saved state in a transaction rather than rollback everything happened in the transaction.

```groovy
$DSL.script {
    
    TRANSACTION {
        // do some stuff here...

        // create a checkpoint and save it to a variable
        def state1 = CHECKPOINT()

        try {
            // do some stuff which may fail
        } catch (Exception ex) {
            // if failed rollback to saved state
            ROLLBACK(state1)
        }

        // must call COMMIT() manually at the end
        COMMIT()
    }

}
```