/**
 * @author IWEERARATHNA
 */
def innerImportPart = $DSL.$q {
    EXPECT (Actor.alias("ac"))

    FETCH (ac.id, ac.title)
}

def innerImportImport = $DSL.$q {
    EXPECT (Actor.alias("ac"))

    FETCH ($IMPORT("projection/import_part"))
}

[
        $DSL.select {
            TARGET (Actor.alias("ac"))
            FETCH (innerImportImport)
        },
        [
            mysql: "SELECT ac.id, ac.title FROM `Actor` ac"
        ],

        $DSL.select {
            TARGET (Actor.alias("ac"))
            FETCH ($IMPORT("projection/import_part"))
        },
        [
            mysql: "SELECT ac.id, ac.title FROM `Actor` ac"
        ],

        $DSL.select {
            TARGET (Actor.alias("ac"))
            FETCH (ac.year, $IMPORT("projection/import_part"))
        },
        [
            mysql: "SELECT ac.year, ac.id, ac.title FROM `Actor` ac"
        ],

        $DSL.select {
            TARGET (Actor.alias("ac"))
            FETCH ($IMPORT("projection/import_part"), ac.year)
        },
        [
            mysql: "SELECT ac.id, ac.title, ac.year FROM `Actor` ac"
        ],

        $DSL.select {
            TARGET (Actor.alias("ac"))
            FETCH (ac.birthDate, $IMPORT("projection/import_part"), ac.year)
        },
        [
            mysql: "SELECT ac.birthDate, ac.id, ac.title, ac.year FROM `Actor` ac"
        ],

        $DSL.select {
            TARGET (Actor.alias("ac"))
            FETCH (innerImportPart)
        },
        [
            mysql: "SELECT ac.id, ac.title FROM `Actor` ac"
        ],

        $DSL.select {
            TARGET (Actor.alias("ac"))
            FETCH (ac.year, innerImportPart)
        },
        [
            mysql: "SELECT ac.year, ac.id, ac.title FROM `Actor` ac"
        ],

        $DSL.select {
            TARGET (Actor.alias("ac"))
            FETCH ($IMPORT('projection/import_part2'))
        },
        [
            mysql: "SELECT ac.id, IFNULL(ac.year, 2016) AS thisYear, ac.title AS nickName FROM `Actor` ac"
        ],

        $DSL.select {
            TARGET (Actor.alias("ac"))
            FETCH ($IMPORT('projection/import_part2'))
            GROUP_BY (thisYear)
        },
        [
            mysql: "SELECT ac.id, IFNULL(ac.year, 2016) AS thisYear, ac.title AS nickName FROM `Actor` ac GROUP BY thisYear"
        ],
]