package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.exceptions.NyException

/**
 * Interface to implement when database needs bootstrap time.
 *
 * @author iweerarathna
 */
interface QDbBootstrappable {

    QScriptList getBootstrapScripts(QSession session) throws NyException

}