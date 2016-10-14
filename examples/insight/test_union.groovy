/**
 * Created by IWEERARATHNA on 9/28/2016.
 */
def q1 = $DSL.$IMPORT("insight/codebranch")
//def q2 = $DSL.$IMPORT("insight/unmapped_users")
$DSL.select {

    TARGET (Simple.alias("sim"))

    JOIN (TARGET()) {
        INNER_JOIN (TABLE(q1).alias("d")) ON (d.id, sim.id)

    }

}