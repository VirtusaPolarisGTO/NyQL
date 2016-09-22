import java.sql.JDBCType

/**
 * @author Isuru Weerarathna
 */
def vals = $SESSION

$DSL.select {

    TARGET (Album.alias("alb"))

    JOINING {
        $IMPORT("partials/joining")
    }

    //INTO(ThisYearSongs)

    /*
    FETCH (alb,
            IFNULL(alb.name, STR("yes")).alias("xxx"),
            DATE_ADD(alb.release, 7, "day"),
            CONCAT(alb.artist, STR(" - "), alb.year),
            COUNT(alb.songs).alias("total"),
            CASE {
                WHEN { EQ alb.id, "0" } THEN { "(s.intr - s.fixed) / v.churn * 100" }
                WHEN { EQ alb.id, "2040" } THEN { "axx" }
                ELSE { "aaa" }
            }.alias("yeah")
    )
    */
    //FETCH ()


    WHERE {
        ALL {
            EQ (alb.name, s.name)
            EQ (alb.year, art.year)
        }

        OR()
        ON (alb.name, "=", P("xxx"))
        AND()
        $IMPORT "partials/where"
        AND()
        $IMPORT "partials/where"
        AND()
        NOT_NULL (alb.genre)
        AND()
        EQ (alb.x, P("abc", JDBCType.INTEGER))
        AND()
        EQ (alb.y, 1023)
    }


    GROUP_BY(alb.year, alb.artist) 
    HAVING {
        ON (COUNT(), ">", alb.year)
    }
    

    //ORDER_BY ( alb.atrist, ASC(alb.year), total )

    TOP 10

    //DATA (isuru: "hellow", "pass": true )
}

