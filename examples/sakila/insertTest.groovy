import com.sun.org.apache.bcel.internal.generic.RETURN

/**
 * @author IWEERARATHNA
 */
$DSL.insert {

    TARGET (Actor)

    DATA (
            "first_name": STR("Isuru"),
            "last_name": STR("Weerarathna"),
            "last_update": NOW()
    )

    RETURN_KEYS()

}

$DSL.update {

    TARGET (Actor.alias("ac"))

    SET {
        EQ (ac.first_name, STR("Isuru"))
    }

    WHERE {
        EQ (ac.first_name, STR("IsuruX"))
    }

    RETURN_KEYS()
}
