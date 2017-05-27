package com.virtusa.gto.nyql

import com.virtusa.gto.nyql.model.QScriptList
import com.virtusa.gto.nyql.model.QSession
import groovy.transform.CompileStatic
/**
 * @author iweerarathna
 */
@CompileStatic
interface MultiQuery {

    QScriptList createScripts(QContext qContext, QSession qSession) throws Exception;

}
