/**
 * @author IWEERARATHNA
 */
def innerImportPart = $DSL.$q {
    EXPECT (Actor.alias("ac"))

    FETCH (ac.id, ac.title)
}

[
        $DSL.select {
            TARGET (Actor.alias("ac"))
            FETCH ($IMPORT("projection/import_part"))
        },
        "SELECT ac.id, ac.title FROM `Actor` ac",

        $DSL.select {
            TARGET (Actor.alias("ac"))
            FETCH (ac.year, $IMPORT("projection/import_part"))
        },
        "SELECT ac.year, ac.id, ac.title FROM `Actor` ac",

        $DSL.select {
            TARGET (Actor.alias("ac"))
            FETCH ($IMPORT("projection/import_part"), ac.year)
        },
        "SELECT ac.id, ac.title, ac.year FROM `Actor` ac",

        $DSL.select {
            TARGET (Actor.alias("ac"))
            FETCH (ac.birthDate, $IMPORT("projection/import_part"), ac.year)
        },
        "SELECT ac.birthDate, ac.id, ac.title, ac.year FROM `Actor` ac",

        $DSL.select {
            TARGET (Actor.alias("ac"))
            FETCH (innerImportPart)
        },
        "SELECT ac.id, ac.title FROM `Actor` ac",

        $DSL.select {
            TARGET (Actor.alias("ac"))
            FETCH (ac.year, innerImportPart)
        },
        "SELECT ac.year, ac.id, ac.title FROM `Actor` ac",
]