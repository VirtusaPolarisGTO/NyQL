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
        [
            mysql: "SELECT * FROM `Film` f INNER JOIN `Actor` ac ON f.actor_id = ac.actor_id"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                $IMPORT ("joins/import_talias")
            }
            FETCH ()
        },
         [
            mysql:  "SELECT * FROM `Film` f " +
                 "INNER JOIN `Actor` ac ON f.actor_id = ac.actor_id " +
                 "INNER JOIN `Payment` p ON ac.payment_id = p.payment_id"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                INNER_JOIN (Payment.alias("p")) ON p.payment_id, f.payment_id
                $IMPORT_UNSAFE ("joins/import_talias123")
            }
            FETCH ()
        },
        [
            mysql: "SELECT * FROM `Film` f INNER JOIN `Payment` p ON p.payment_id = f.payment_id"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                $IMPORT_UNSAFE ("joins/import_talias123")
            }
            FETCH ()
        },
        [
            mysql: "SELECT * FROM `Film` f"
        ]
]
