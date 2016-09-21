import java.sql.JDBCType

$DSL.update {

    // Target table to update
    TARGET Album.alias("alb")

    // Join update. This clause if optional.
    // If stated, then the first table must be as same as TARGET table.
    JOINING {
        TABLE("alb") JOIN Song.alias("s") ON alb.id, s.id JOIN Artist.alias("art") ON art.id, s.id
    }

    // Collection of values to be set
    // Refer the 'Assign' class for available functions.
    SET {
        EQ alb.name, STR("isuru")
        EQ alb.year, P("year", JDBCType.INTEGER)
        EQ alb.ryear, P("year", JDBCType.INTEGER)
        SET_NULL alb.details
        EQ alb.sellers, IMPORT("insert")
    }

    // Set of conditions.
    WHERE {
        ALL {
            EQ alb.id, P("albid", JDBCType.INTEGER)
        }
    }

}