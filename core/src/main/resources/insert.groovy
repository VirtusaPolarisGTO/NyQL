import java.sql.JDBCType

$DSL.insert {

    // which table to insert data
    TARGET Song.alias("s")

    // A set of column values to be set.
    // Format:
    //      <column-name> : <value>
    //
    DATA (
            "id": P("id", JDBCType.INTEGER),
            "name": P("str", JDBCType.VARCHAR)
    )

}