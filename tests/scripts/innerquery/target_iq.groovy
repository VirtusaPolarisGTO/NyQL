/**
 * @author IWEERARATHNA
 */

def innQ = $DSL.select {
    TARGET (Film.alias("f"))
    FETCH ($IMPORT("innerquery/import_fetch"))
}

[
    $DSL.select {
        TARGET (TABLE(innQ).alias("ac"))
        FETCH ()
    },
    "SELECT * FROM (SELECT f.id, f.year FROM `Film` f) ac",

    $DSL.select {
        TARGET (TABLE($IMPORT("innerquery/other_query")).alias("iq"))
    },
    "SELECT * FROM (SELECT * FROM `Film` f) iq",

    $DSL.select {
        TARGET (TABLE($IMPORT("innerquery/other_query")).alias("iq"))
    },
    "SELECT * FROM (SELECT * FROM `Film` f) iq"
]