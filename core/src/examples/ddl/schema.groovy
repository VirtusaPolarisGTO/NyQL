import com.virtusa.gto.insight.nyql.ddl.DFieldType
import com.virtusa.gto.insight.nyql.ddl.DKeyType

/**
 * @author IWEERARATHNA
 */
$DSL.ddl {

    TABLE("Violation") {

        FIELD { name: "id" }
        //FIELD("id",             DFieldType.BIGINT,      [sequence: true, notNull: true])
        //FIELD("hashCode",       DFieldType.TEXT,        [notNull: true])

        //KEY()
    }

    /*
    TABLE("CodeUnit") {

        FIELD("id",             DFieldType.BIGINT,      [sequence: true, notNull: true])
        FIELD("name",           DFieldType.TEXT,        [notNull: true])

        KEY("primary", DKeyType.PRIMARY, ["id"], "Violation", "id")
    }
    */

}