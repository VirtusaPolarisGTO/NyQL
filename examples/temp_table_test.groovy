// creating temp table ddl query
def tempCreate = $DSL.ddl {

    TEMP_TABLE ("Sample_Temp_Table") {
        FIELD ("id", DFieldType.INT)
    }

}

// dropping temp table ddl query
def tempDrop = $DSL.ddl {
    DROP_TEMP_TABLE ("Sample_Temp_Table")
}

// using 'sakila' sample database, insert all film ids to sample temp table
def selIns = $DSL.select {

    TARGET (Film.alias("f"))

    FETCH (f.film_id)

    INTO (TABLE("Sample_Temp_Table"))

}

// select all from temp table
def selQ = $DSL.select {

    TARGET (TABLE("Sample_Temp_Table").alias("stemp"))

    FETCH ()
}

$DSL.script {

    RUN(tempCreate)
    RUN(selIns)

    // run the script and get that into a variable
    def r = RUN(selQ)

    // use $LOG to print something in the console
    $LOG r

    // run drop script
    RUN(tempDrop)


}
