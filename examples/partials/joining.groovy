/**
 * @author IWEERARATHNA
 */
$DSL.$q {

    EXPECT (Album.alias("alb"))

    JOIN (alb) {
        INNER_JOIN (Song.alias("s")) ON "alb.id = s.id"
        INNER_JOIN (Artist.alias("art")) ON {
                ALL {
                    NEQ (alb.id, s.id)
                    EQ (alb.sid, s.id)
                }
            }
    }

}