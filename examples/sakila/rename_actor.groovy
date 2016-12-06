/**
 * Update actor who's having first name is 'KENNETH' to 'Keneth'
 */
$DSL.update {

    TARGET (Actor.alias("ac"))

    SET {
        EQ (ac.first_name, STR("Keneth"))
    }

    WHERE {
        EQ (ac.first_name, STR("Kenneth"))
    }

}
