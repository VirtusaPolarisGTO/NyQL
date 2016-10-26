import com.virtusa.gto.nyql.ddl.DFieldType

/**
 * @author IWEERARATHNA
 */
$DSL.ddl {
    TEMP_TABLE ("Film") {
        FIELD ("id", DFieldType.INT, [sequence: true, notNull: true])
        FIELD ("title", DFieldType.BOOLEAN, [notNull: true])
        FIELD ("mainLanguage", DFieldType.TEXT, [defaultValue: "English"])
        FIELD ("ticketPrice", DFieldType.DOUBLE)
        FIELD ("movieId", DFieldType.BIGINT)
    }
}
