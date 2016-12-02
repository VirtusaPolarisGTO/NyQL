/**
 * @author IWEERARATHNA
 */
[
    $DSL.bulkInsert {
        TARGET (Customer.alias("c"))
        DATA (
            "name": PARAM("custName"),
            "age": NUM(25),
            "email": PARAM("custEmail")
        )
    },
    [
        mysql:  ["INSERT INTO `Customer` (`name`, `age`, `email`) VALUES (?, 25, ?)", ["custName", "custEmail"]],
        pg:     ['INSERT INTO "Customer" ("name", "age", "email") VALUES (?, 25, ?)', ["custName", "custEmail"]],
    ]

]