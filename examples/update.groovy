
/**
 * @author IWEERARATHNA
 */

$DSL.update {

    TARGET (Album.alias("alb"))

    JOIN (TARGET()) {
        INNER_JOIN (Song.alias("s")) ON alb.id, s.id
        INNER_JOIN (Artist.alias("art")) ON art.id, s.id
    }


    SET {
        EQ (alb.name, STR("isuru"))
        EQ (alb.year, PARAM("year"))
        EQ (alb.ryear, PARAM("year"))
        SET_NULL (alb.details)
        EQ (alb.sellers, $IMPORT("insert"))
    }

    WHERE {
        ALL {
            EQ (alb.id, PARAM("albid"))
        }
    }

}

