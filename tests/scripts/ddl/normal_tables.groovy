import com.virtusa.gto.nyql.ddl.DFieldType
import com.virtusa.gto.nyql.ddl.DKeyIndexType

/**
 * @author IWEERARATHNA
 */
[
        $DSL.ddl {
            TABLE ("Film") {
                FIELD ("id", DFieldType.INT, [sequence: true, notNull: true])
                FIELD ("title", DFieldType.BOOLEAN, [notNull: true])
                FIELD ("mainLanguage", DFieldType.TEXT, [defaultValue: "English"])
                FIELD ("ticketPrice", DFieldType.DOUBLE)
                FIELD ("movieId", DFieldType.BIGINT)
            }
        },
        [
            [mysql:  "CREATE TABLE `Film`(" +
                         "`id` INT NOT NULL AUTO_INCREMENT, " +
                         "`title` TINYINT(1) NOT NULL, " +
                         "`mainLanguage` TEXT DEFAULT 'English', " +
                         "`ticketPrice` DOUBLE, " +
                         "`movieId` BIGINT" +
                         ")"
            ]
        ],

        $DSL.ddl {
            DROP_TABLE ("Film")
        },
        [[mysql:  "DROP TABLE `Film`"]],

        $DSL.ddl {
            DROP_TABLE ("Film", true)
        },
        [[mysql: "DROP TABLE IF EXISTS `Film`"]],

        $DSL.ddl {
            TABLE ("Film") {
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
        [[mysql:  "CREATE TABLE `Film`(" +
                 "`id` INT NOT NULL AUTO_INCREMENT, " +
                 "`title` TINYINT(1) NOT NULL, " +
                 "`mainLanguage` TEXT DEFAULT 'English', " +
                 "`ticketPrice` DOUBLE, " +
                 "`movieId` BIGINT, " +
                 "PRIMARY KEY (`id`), " +
                 "KEY `idx_film_title` (`title`) USING BTREE, " +
                 "CONSTRAINT `fk_film_movie_movieId` FOREIGN KEY (`movieId`) REFERENCES Movie(`id`)" +
                 ")"
        ]],

        $DSL.ddl {
            TABLE ("Film", true) {
                FIELD ("id", DFieldType.INT, [sequence: true, notNull: true])
                FIELD ("title", DFieldType.BOOLEAN, [notNull: true])
                FIELD ("mainLanguage", DFieldType.TEXT, [defaultValue: "English"])
                FIELD ("ticketPrice", DFieldType.DOUBLE)
                FIELD ("movieId", DFieldType.BIGINT)

                PRIMARY_KEY ("id", "movieId")
            }
        },
        [[mysql:  "CREATE TABLE IF NOT EXISTS `Film`(" +
                 "`id` INT NOT NULL AUTO_INCREMENT, " +
                 "`title` TINYINT(1) NOT NULL, " +
                 "`mainLanguage` TEXT DEFAULT 'English', " +
                 "`ticketPrice` DOUBLE, " +
                 "`movieId` BIGINT, " +
                 "PRIMARY KEY (`id`, `movieId`)" +
                 ")"
        ]],
]