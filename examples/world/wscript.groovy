/**
 * @author IWEERARATHNA
 */
def s = $SESSION

$DSL.script {

    Random random = new Random()
    def result = RUN("world/find")
    long startx = System.currentTimeMillis()
    for (int i = 0; i < 10; i++) {

        int p = random.nextInt(s["allCnts"].size())
        s["aus"] = s["allCnts"][p]
        long start = System.currentTimeMillis()
        result = RUN("world/find")
        $LOG "Elapsed: " + (System.currentTimeMillis() - start) + " ms"

        $LOG result
    }
    //println("Elapsed: " + (System.currentTimeMillis() - startx) + " ms")
    return result
}