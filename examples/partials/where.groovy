/**
 * @author IWEERARATHNA
 */
$DSL.$q {

    EXPECT (Song.alias("s"))
    EXPECT (Album.alias("alb"))

    WHERE {
        BETWEEN (alb.year, s.year, s.nextyear)
        OR()
        LIKE (alb.genre, STR("%hello%"))
        AND()
        EQ (alb.title, PARAM("insideP"))
    }


}