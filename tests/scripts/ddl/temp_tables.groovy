import com.virtusa.gto.insight.nyql.ddl.DFieldType
import com.virtusa.gto.insight.nyql.ddl.DKeyIndexType

/**
 * @author IWEERARATHNA
 */
[
        $DSL.ddl {
            TEMP_TABLE ("Film") {
                FIELD ("id", DFieldType.INT, [sequence: true, notNull: true])
                FIELD ("title", DFieldType.BOOLEAN, [notNull: true])
                FIELD ("mainLanguage", DFieldType.TEXT, [defaultValue: "English"])
                FIELD ("ticketPrice", DFieldType.DOUBLE)
                FIELD ("movieId", DFieldType.BIGINT)
            }
        },
        ["CREATE TEMPORARY TABLE `Film`(" +
                 "`id` INT NOT NULL AUTO_INCREMENT, " +
                 "`title` TINYINT(1) NOT NULL, " +
                 "`mainLanguage` TEXT DEFAULT 'English', " +
                 "`ticketPrice` DOUBLE, " +
                 "`movieId` BIGINT" +
                 ")"
        ],

        $DSL.ddl {
            DROP_TEMP_TABLE ("Film")
        },
        ["DROP TEMPORARY TABLE `Film`"],

        $DSL.ddl {
            TEMP_TABLE ("Film") {
                FIELD ("id", DFieldType.INT, [sequence: true, notNull: true])
                FIELD ("title", DFieldType.BOOLEAN, [notNull: true])
                FIELD ("mainLanguage", DFieldType.TEXT, [defaultValue: "English"])
                FIELD ("ticketPrice", DFieldType.DOUBLE)
                FIELD ("movieId", DFieldType.BIGINT)

                PRIMARY_KEY ("id")

                INDEX ("idx_film_title", [fields: ["title"], indexType: DKeyIndexType.BTREE, unique: true])
                FOREIGN_KEY ("fk_film_movie_movieId", "movieId", [refTable: "Movie", refFields: ["id"]])
            }
        },
        ["CREATE TEMPORARY TABLE `Film`(" +
                 "`id` INT NOT NULL AUTO_INCREMENT, " +
                 "`title` TINYINT(1) NOT NULL, " +
                 "`mainLanguage` TEXT DEFAULT 'English', " +
                 "`ticketPrice` DOUBLE, " +
                 "`movieId` BIGINT, " +
                 "PRIMARY KEY (`id`), " +
                 "KEY `idx_film_title` (`title`) USING BTREE, " +
                 "CONSTRAINT `fk_film_movie_movieId` FOREIGN KEY (`movieId`) REFERENCES Movie(`id`)" +
                 ")"
        ],

        $DSL.ddl {
            TEMP_TABLE ("Film") {
                FIELD ("id", DFieldType.INT, [sequence: true, notNull: true])
                FIELD ("title", DFieldType.BOOLEAN, [notNull: true])
                FIELD ("mainLanguage", DFieldType.TEXT, [defaultValue: "English"])
                FIELD ("ticketPrice", DFieldType.DOUBLE)
                FIELD ("movieId", DFieldType.BIGINT)

                PRIMARY_KEY (["id", "movieId"])
            }
        },
        ["CREATE TEMPORARY TABLE `Film`(" +
                 "`id` INT NOT NULL AUTO_INCREMENT, " +
                 "`title` TINYINT(1) NOT NULL, " +
                 "`mainLanguage` TEXT DEFAULT 'English', " +
                 "`ticketPrice` DOUBLE, " +
                 "`movieId` BIGINT, " +
                 "PRIMARY KEY (`id`, `movieId`)" +
                 ")"
        ],
]