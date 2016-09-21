/**
 * @author IWEERARATHNA
 */
$DSL.$q {

    EXPECT (Song.alias("s"))
    EXPECT (Album.alias("alb"))

    WHERE {
        ON (alb.year, BETWEEN(s.year, s.nextyear))
        OR()
        ON (alb.genre, LIKE(STR("%hello%")))
        AND()
        ON (alb.title, "=", P("insideP"))
    }


}