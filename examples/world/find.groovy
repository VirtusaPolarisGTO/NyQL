import java.sql.JDBCType

/**
 * @author IWEERARATHNA
 */
def vals = $SESSION

$DSL.select {
/*
    TARGET SampleCity.alias('sc')

    FETCH ()
*/

    TARGET (Country.alias("c"))

    JOIN (TARGET()) {
        INNER_JOIN (City.alias("ct")) ON ct.CountryCode, c.Code
    }

    FETCH (ct.District,
            COUNT().alias("total")
//            CONCAT(ct.District, STR(" "), ct.Name).alias("fullName"),
//            CASE {
//                WHEN { ON (ct.ID, ">", 135) } THEN { ct.ID }
//                ELSE { 0 }
//            }.alias("aaa")
    )

    WHERE {
        EQ (c.Code, PARAM("aus"))
    }

    GROUP_BY (ct.District)
    HAVING {
        GT (COUNT(), 1)
    }

    ORDER_BY (DESC(total))


}