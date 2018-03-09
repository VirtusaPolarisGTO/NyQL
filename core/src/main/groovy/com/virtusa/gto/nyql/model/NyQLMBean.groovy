package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.exceptions.NyException

/**
 * @author iweerarathna
 */
interface NyQLMBean {

    String executeToJSON(String scriptName, Map<String, Object> data) throws NyException

    Object executeScript(String scriptName, Map<String, Object> data) throws NyException

    void recompileScript(String scriptName) throws NyException

    QScript parse(String scriptName, Map<String, Object> data) throws NyException

}
