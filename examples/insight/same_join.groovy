/**
 * Created by IWEERARATHNA on 9/28/2016.
 */
def jq = $DSL.$q {
    EXPECT (TABLE("Song").alias("s2"))

    JOIN {
        TABLE("s2") INNER_JOIN (TABLE("Album").alias("alb")) ON (alb.id, s2.id)
    }
}

$DSL.select {

    TARGET (Song.alias("s"))

    JOIN {
        TARGET() INNER_JOIN TABLE("Song").alias("s2") ON (s.id, s2.id)
    }

}
