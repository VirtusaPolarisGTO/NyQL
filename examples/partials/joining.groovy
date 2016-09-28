/**
 * @author IWEERARATHNA
 */
$DSL.$q {

    EXPECT (Album.alias("alb"))

    JOINING {
        Album.alias("alb") INNER_JOIN Song.alias("s") ON "alb.id = s.id" \
            INNER_JOIN Artist.alias("art") ON {
                ALL {
                    ON (alb.id, "<>", s.id)
                    EQ (alb.sid, s.id)
                }
            }
    }

}