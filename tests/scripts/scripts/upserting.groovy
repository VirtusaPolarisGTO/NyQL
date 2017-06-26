
def q = $DSL.upsert {
    TARGET (Film.alias("f"))
    SET {
        EQ (f.film_id, 1234)
        EQ (f.title, PARAM("title"))
        SET_NULL (f.language_id)
    }
    WHERE {
        GT (f.year, 2010)
    }
}

def q2 = $DSL.upsert {
    TARGET (Film.alias("f"))
    SET {
        EQ (f.film_id, 1234)
        EQ (f.title, PARAM("title"))
        SET_NULL (f.language_id)
    }
    WHERE {
        GT (f.year, 2010)
    }
    RETURN_BEFORE()
}

def q3 = $DSL.upsert {
    TARGET (Film.alias("f"))
    SET {
        EQ (f.film_id, 1234)
        EQ (f.title, PARAM("title"))
        SET_NULL (f.language_id)
    }
    WHERE {
        GT (f.year, 2010)
    }
    RETURN_AFTER()
}

$DSL.script {

    $LOG "Running UPSERT query without returning anything."
    RUN (q)

    $LOG ""
    $LOG "Running UPSERT query with returning before"
    RUN (q2)

    $LOG ""
    $LOG "Running UPSERT query with lastest result"
    RUN (q3)
}