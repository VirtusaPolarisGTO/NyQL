import com.virtusa.gto.insight.nyql.utils.QueryType

/**
 * @author IWEERARATHNA
 */
$DSL.nativeQuery(
        QueryType.SELECT,
        [
            mysql:
                """
                SELECT * FROM MySQLDatabase d WHERE d.year = 2016
                """,
            pg:
                """
                SELECT * FROM PostgreDB d WHERE d.year = 2016
                """
        ]
)