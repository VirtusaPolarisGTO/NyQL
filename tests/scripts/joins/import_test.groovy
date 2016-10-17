/**
 * @author IWEERARATHNA
 */
[
        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN (TARGET()) {
                $IMPORT ("joins/import_talias")
            }
            FETCH ()
        },
         "SELECT * FROM `Film` f " +
                 "INNER JOIN `Actor` ac ON f.actor_id = ac.actor_id "
]
