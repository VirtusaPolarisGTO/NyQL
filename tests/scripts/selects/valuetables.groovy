/**
 * @author iweerarathna
 */
[
        $DSL.valueTable([1, 2, 3]),
        [
                mysql: "SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3"
        ],

        $DSL.valueTable([1, 2, 3], 'No'),
        [
                mysql: "SELECT 1 AS `No` UNION ALL SELECT 2 UNION ALL SELECT 3"
        ],

        $DSL.valueTable([1, 2, 3], 'MyColumn'),
        [
                mysql: "SELECT 1 AS MyColumn UNION ALL SELECT 2 UNION ALL SELECT 3"
        ],

        $DSL.valueTable([[a: 1, b: 2], [a: 3, b: 4]]),
        [
                mysql: "SELECT 1 AS a, 2 AS b UNION ALL SELECT 3, 4"
        ],

        $DSL.select {
            TARGET (TABLE([1, 2, 3, 4], 'quarter').alias("qTbl"))
            JOIN {
                LEFT_JOIN (Payroll.alias('pr')) ON pr.quarter, qTbl.quarter
            }
        },
        [
                mysql: "SELECT * FROM " +
                        "(SELECT 1 AS `quarter` UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) qTbl " +
                        "LEFT JOIN `Payroll` pr ON pr.quarter = qTbl.quarter"
        ],

        $DSL.select {
            TARGET (TABLE(STR_LIST(["1st", "2nd", "3rd", "4th"]), 'quarter').alias("qTbl"))
            JOIN {
                LEFT_JOIN (Payroll.alias('pr')) ON pr.quarter, qTbl.quarter
            }
        },
        [
                mysql: "SELECT * FROM " +
                        "(SELECT \"1st\" AS `quarter` UNION ALL SELECT \"2nd\" UNION ALL SELECT \"3rd\" UNION ALL SELECT \"4th\") qTbl " +
                        "LEFT JOIN `Payroll` pr ON pr.quarter = qTbl.quarter"
        ]

]