import java.sql.JDBCType

/**
 * @author IWEERARATHNA
 */

def insQ = $DSL.insert {

    TARGET (Song.alias("s"))

    DATA (
            "id": P("id", JDBCType.INTEGER),
            "name": P("str", JDBCType.VARCHAR)
    )

}

$DSL.update {

    TARGET (Album.alias("alb"))

    JOINING {
        TABLE("alb") JOIN Song.alias("s") ON alb.id, s.id JOIN Artist.alias("art") ON art.id, s.id
    }


    SET {
        EQ (alb.name, STR("isuru"))
        EQ (alb.year, P("year", JDBCType.INTEGER))
        EQ (alb.ryear, P("year", JDBCType.INTEGER))
        SET_NULL (alb.details)
        EQ (alb.sellers, IMPORT("insert"))
    }

    WHERE {
        ALL {
            EQ (alb.id, P("albid", JDBCType.INTEGER))
        }
    }

}

