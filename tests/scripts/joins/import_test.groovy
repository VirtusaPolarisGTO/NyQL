/**
 * @author IWEERARATHNA
 */
[
        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                $IMPORT ("joins/import_talias2")
            }
            FETCH ()
        },
        "SELECT * FROM `Film` f " +
                "INNER JOIN `Actor` ac ON f.actor_id = ac.actor_id",

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                $IMPORT ("joins/import_talias")
            }
            FETCH ()
        },
         "SELECT * FROM `Film` f " +
                 "INNER JOIN `Actor` ac ON f.actor_id = ac.actor_id " +
                 "INNER JOIN `Payment` p ON ac.payment_id = p.payment_id",

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                $IMPORT_UNSAFE ("joins/import_talias123")
            }
            FETCH ()
        },
        "SELECT * FROM `Film` f"
]
