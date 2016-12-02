/**
 * @author IWEERARATHNA
 */
[
    $DSL.bulkUpdate {
        TARGET (UserRental.alias("ur"))
        SET {
            EQ (ur.current_rental, PARAM("currentRental"))
        }
        WHERE {
            EQ (ur.user_id, PARAM("theUser"))
        }
    },
    [
        mysql:  ["UPDATE `UserRental` ur SET ur.current_rental = ? WHERE ur.user_id = ?", ["currentRental", "theUser"]],
        pg:     ['UPDATE "UserRental" ur SET ur.current_rental = ? WHERE ur.user_id = ?', ["currentRental", "theUser"]]
    ]
]