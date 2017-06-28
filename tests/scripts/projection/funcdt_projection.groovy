
/**
 * @author IWEERARATHNA
 */
[
    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (NOW(), CURRENT_DATE())
    },
    [
        mysql: "SELECT NOW(), CURDATE() FROM `Actor` ac"
    ],

    $DSL.select {
        FETCH (CURRENT_TIME(), CURRENT_TIME().alias("nowTime"), CURRENT_EPOCH())
    },
    [
        mysql: "SELECT CURTIME(), CURTIME() AS nowTime, UNIX_TIMESTAMP() * 1000"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (DATE_TRUNC(ac.birthTimestamp), DATE_TRUNC(ac.updatedAt).alias("lastUpdated"))
    },
    [
        mysql: "SELECT DATE(ac.birthTimestamp), DATE(ac.updatedAt) AS lastUpdated FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (EPOCH_TO_DATE(ac.birthEpoch), EPOCH_TO_DATETIME(ac.updatedAt).alias("lastUpdated"),
            EPOCH_TO_DATE(PARAM("birthTimeEpoch")))
    },
    [
        mysql: ["SELECT DATE(FROM_UNIXTIME(ac.birthEpoch / 1000)), FROM_UNIXTIME(ac.updatedAt / 1000) AS lastUpdated, " +
                "DATE(FROM_UNIXTIME(? / 1000)) " +
                "FROM `Actor` ac", ["birthTimeEpoch"]]
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (DATE_DIFF_YEARS(ac.birthDate, CURRENT_DATE()))
        FETCH (DATE_DIFF_MONTHS(ac.birthDate, ac.updatedAt))
        FETCH (DATE_DIFF_WEEKS(ac.birthDate, CURRENT_DATE()))
        FETCH (DATE_DIFF_DAYS(ac.birthDate, ac.updatedAt))
        FETCH (DATE_DIFF_HOURS(ac.birthDate, CURRENT_DATE()))
        FETCH (DATE_DIFF_MINUTES(ac.birthDate, PARAM("otherDate")))
        FETCH (DATE_DIFF_SECONDS(ac.birthDate, CURRENT_DATE()))
    },
    [
        mysql: ["SELECT TIMESTAMPDIFF(YEAR, ac.birthDate, CURDATE()), " +
                "TIMESTAMPDIFF(MONTH, ac.birthDate, ac.updatedAt), " +
                "TIMESTAMPDIFF(WEEK, ac.birthDate, CURDATE()), " +
                "TIMESTAMPDIFF(DAY, ac.birthDate, ac.updatedAt), " +
                "TIMESTAMPDIFF(HOUR, ac.birthDate, CURDATE()), " +
                "TIMESTAMPDIFF(MINUTE, ac.birthDate, ?), " +
                "TIMESTAMPDIFF(SECOND, ac.birthDate, CURDATE()) " +
                "FROM `Actor` ac", ["otherDate"]]
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (DATE_ADD_DAYS(ac.birthDate, ac.error))
        FETCH (DATE_ADD_HOURS(ac.birthDate, 24))
        FETCH (DATE_ADD_MINUTES(ac.birthDate, ac.error))
        FETCH (DATE_ADD_SECONDS(ac.birthDate, ac.error))
        FETCH (DATE_ADD_WEEKS(ac.birthDate, 7))
        FETCH (DATE_ADD_MONTHS(ac.birthDate, PARAM("addMonths")))
        FETCH (DATE_ADD_YEARS(ac.birthDate, NUM(1)))
    },
    [
        mysql: ["SELECT (ac.birthDate + INTERVAL ac.error DAY), " +
             "(ac.birthDate + INTERVAL 24 HOUR), " +
             "(ac.birthDate + INTERVAL ac.error MINUTE), " +
             "(ac.birthDate + INTERVAL ac.error SECOND), " +
             "(ac.birthDate + INTERVAL 7 WEEK), " +
             "(ac.birthDate + INTERVAL ? MONTH), " +
             "(ac.birthDate + INTERVAL 1 YEAR) " +
             "FROM `Actor` ac", ["addMonths"]]
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (DATE_SUB_DAYS(ac.birthDate, ac.error))
        FETCH (DATE_SUB_HOURS(ac.birthDate, 24))
        FETCH (DATE_SUB_MINUTES(ac.birthDate, ac.error))
        FETCH (DATE_SUB_SECONDS(ac.birthDate, ac.error))
        FETCH (DATE_SUB_WEEKS(ac.birthDate, 7))
        FETCH (DATE_SUB_MONTHS(ac.birthDate, PARAM("addMonths")))
        FETCH (DATE_SUB_YEARS(ac.birthDate, NUM(1)))
    },
    [
        mysql: ["SELECT (ac.birthDate - INTERVAL ac.error DAY), " +
             "(ac.birthDate - INTERVAL 24 HOUR), " +
             "(ac.birthDate - INTERVAL ac.error MINUTE), " +
             "(ac.birthDate - INTERVAL ac.error SECOND), " +
             "(ac.birthDate - INTERVAL 7 WEEK), " +
             "(ac.birthDate - INTERVAL ? MONTH), " +
             "(ac.birthDate - INTERVAL 1 YEAR) " +
             "FROM `Actor` ac", ["addMonths"]]
    ],
]