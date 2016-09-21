package com.virtusa.gto.insight.nyql.model

import com.virtusa.gto.insight.nyql.QResultProxy
import groovy.transform.ToString

/**
 * @author IWEERARATHNA
 */
@ToString(excludes = ["qSession"])
class QScript {

    QResultProxy proxy
    //GroovyShell containerShell
    QSession qSession


}
