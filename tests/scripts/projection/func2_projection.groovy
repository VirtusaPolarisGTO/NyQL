
/**
 * @author IWEERARATHNA
 */
[
    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id, MINUS(ac.income, ac.tax),
                MINUS(ac.income, 2), MINUS(2, ac.income), MINUS(1, 2))
    },
    [
        mysql: "SELECT ac.id, (ac.income - ac.tax), (ac.income - 2), (2 - ac.income), (1 - 2) FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id, MULTIPLY(ac.income, ac.incomeFactor),
                MULTIPLY(ac.income, 2), MULTIPLY(2, ac.income), MULTIPLY(2, 3))
    },
    [
        mysql: "SELECT ac.id, (ac.income * ac.incomeFactor), (ac.income * 2), (2 * ac.income), (2 * 3) FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id, DIVIDE(ac.income, ac.familyCount),
                DIVIDE(ac.income, 2), DIVIDE(2, ac.income), DIVIDE(22, 7))
    },
    [
        mysql: "SELECT ac.id, (ac.income / ac.familyCount), (ac.income / 2), (2 / ac.income), (22 / 7) FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id, MODULUS(ac.income, ac.spent),
                MODULUS(ac.income, 2), MODULUS(2, ac.income), MODULUS(22, 7))
    },
    [
        mysql: "SELECT ac.id, (ac.income % ac.spent), (ac.income % 2), (2 % ac.income), (22 % 7) FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id, INVERSE(ac.income))
    },
    [
        mysql: "SELECT ac.id, (1 / ac.income) FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (ac.id, LCASE(ac.name), UCASE(ac.lastName), TRIM(ac.details), LEN(ac.countryCode))
    },
    [
        mysql: "SELECT ac.id, LOWER(ac.name), UPPER(ac.lastName), TRIM(ac.details), CHAR_LENGTH(ac.countryCode) FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (REVERSE(ac.nameHash), LEFT_TRIM(ac.city), RIGHT_TRIM(ac.country))
    },
    [
        mysql: "SELECT REVERSE(ac.nameHash), LTRIM(ac.city), RTRIM(ac.country) FROM `Actor` ac"
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (STR_LEFT(ac.details, 320), STR_LEFT(ac.details, PARAM("showLength")))
        FETCH (STR_RIGHT(ac.details, 100), STR_RIGHT(ac.details, PARAM("showLastLength")))
    },
    [
        mysql: ["SELECT LEFT(ac.details, 320), LEFT(ac.details, ?), " +
                "RIGHT(ac.details, 100), RIGHT(ac.details, ?) " +
                "FROM `Actor` ac", ["showLength", "showLastLength"]]
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (LEFT_PAD(ac.nickName, 10), LEFT_PAD(ac.nickName, 10, STR("-")),
                RIGHT_PAD(ac.surname, 16), RIGHT_PAD(ac.surname, 16, STR("-")),
                RIGHT_PAD(ac.surname, PARAM("padRightParam")), RIGHT_PAD(ac.surname, 16, PARAM("padRightChar")))
    },
    [
        mysql: ["SELECT LPAD(ac.nickName, 10, \" \"), LPAD(ac.nickName, 10, \"-\"), " +
                "RPAD(ac.surname, 16, \" \"), RPAD(ac.surname, 16, \"-\"), " +
                "RPAD(ac.surname, ?, \" \"), RPAD(ac.surname, 16, ?) " +
                "FROM `Actor` ac", ["padRightParam", "padRightChar"]]
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (POWER(ac.rank, 3), POWER(ac.rank, PARAM("toPower")), POWER(2, 3),
                SIGN(ac.debt), SQRT(ac.income),
                DEGREES(ac.latitude), RADIANS(ac.longitude))
    },
    [
        mysql: ["SELECT POWER(ac.rank, 3), POWER(ac.rank, ?), POWER(2, 3), " +
                "SIGN(ac.debt), SQRT(ac.income), DEGREES(ac.latitude), RADIANS(ac.longitude) " +
                "FROM `Actor` ac", ["toPower"]]
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (CAST_INT(ac.birthDay), CAST_STR(ac.level), CAST_DATE(ac.retiredDate),
                CAST_INT(PARAM("strValue")), CAST_STR(PARAM("intValue")), CAST_DATE(PARAM("strDateValue")))
    },
    [
        mysql: ["SELECT CAST(ac.birthDay AS SIGNED), CAST(ac.level AS CHAR), CAST(ac.retiredDate AS DATE), " +
                "CAST(? AS SIGNED), CAST(? AS CHAR), CAST(? AS DATE) " +
                "FROM `Actor` ac", ["strValue", "intValue", "strDateValue"]]
    ],

    $DSL.select {
        TARGET (Actor.alias("ac"))
        FETCH (COALESCE(ac.description, ac.details, STR('(none)')),
                COALESCE(ac.description, PARAM("defVal1"), PARAM("defVal2")))
    },
    [
            mysql: ["SELECT COALESCE(ac.description, ac.details, \"(none)\"), " +
                    "COALESCE(ac.description, ?, ?) " +
                    "FROM `Actor` ac", ["defVal1", "defVal2"]]
    ],
]