import com.virtusa.gto.insight.nyql.ddl.DFieldType
import com.virtusa.gto.insight.nyql.ddl.DKeyIndexType

/**
 * @author IWEERARATHNA
 */
$DSL.script {

    ddl {
        TEMP_TABLE("MyTempTable") {
            // fields
            //////////////////////////////////

            // All supported field values:
            //      sequence:           boolean
            //      notNull:            boolean
            //      length:             integer
            //      unsigned:           boolean
            //      defaultValue:       any
            FIELD("id", DFieldType.INT, [sequence: true, notNull: true, length: 0, unsigned: true, defaultValue: 1])
            FIELD("name", DFieldType.TEXT)
            FIELD("country_id", DFieldType.INT)

            // keys/indexes
            /////////////////////////////////

            // primary key: just give the column name, or list of field names
            PRIMARY_KEY ("id")
            //// PRIMARY_KEY (["id", "other_id"])

            // Indexes
            //  parameter-0:    name of the index
            //
            // All supported index properties
            //      unique:          boolean
            //      fields:          [list of field names]
            //      indexType:       {DKeyIndexType.[BTREE | HASH] }
            INDEX ("idx_name", [fields: ["name"], indexType: DKeyIndexType.HASH])

            // Foreign keys
            //  parameter-0 : name of the foreign key
            //  parameter-1 : field name of this table
            //
            // All supported properties:
            //      refTable:       string
            //      refFields:      [list of fields in other table]
            //      onDelete:       { DReferenceOption.[RESTRICT | CASCADE | SET_NULL | NO_ACTION] }
            //      onUpdate:       { DReferenceOption.[RESTRICT | CASCADE | SET_NULL | NO_ACTION] }
            FOREIGN_KEY ("fk_person_country", "country_id", [refTable: "Country", refFields: ["id"]])
        }
    }

    ddl {
        DROP_TEMP_TABLE ("nameOfTempTable")
    }

}