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

    TARGET Country.alias("c")

    JOINING {
        TARGET() INNER_JOIN City.alias("ct") ON ct.CountryCode, c.Code
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
        EQ (c.Code, P("aus"))
    }

    GROUP_BY (ct.District)
    HAVING {
        ON (COUNT(), ">", 1)
    }

    ORDER_BY (DESC(total))


}