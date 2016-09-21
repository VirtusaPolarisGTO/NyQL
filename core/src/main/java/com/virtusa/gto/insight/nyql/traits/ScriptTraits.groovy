package com.virtusa.gto.insight.nyql.traits

import com.virtusa.gto.insight.nyql.model.QScript

/**
 * @author IWEERARATHNA
 */
trait ScriptTraits {

    def IMPORT(String scriptName) {
        QScript script = _ctx.ownerSession.scriptRepo.parse(scriptName, _ctx.ownerSession)
        return script.proxy
    }

}