package com.virtusa.gto.nyql.model
/**
 * @author iweerarathna
 */
interface NyQLInstanceMXBean {

    String getName()

    String executeToJSON(String scriptName, String dataJson)

    void recompile(String scriptName)

    String parseScript(String scriptName, String dataJson)

}
