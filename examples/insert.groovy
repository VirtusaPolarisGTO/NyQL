

import DSL

import java.sql.JDBCType

/**
 * @author IWEERARATHNA
 */

$DSL.insert {

    TARGET (Song.alias("s"))

    DATA (
            "id": P("id", JDBCType.INTEGER),
            "name": P("str", JDBCType.VARCHAR)
    )

}