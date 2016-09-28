import java.sql.PreparedStatement

/**
 * @author IWEERARATHNA
 */
$DSL.$q {

    EXPECT (Film_Category.alias("fc"))
    EXPECT (Film.alias("f"))
    EXPECT (TABLE("Category").alias("c"))

    JOIN {
        TABLE("f") INNER_JOIN fc ON (f.film_id, fc.film_id) INNER_JOIN c ON c.category_id, fc.category_id
    }

}