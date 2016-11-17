package com.virtusa.gto.nyql.traits

import com.virtusa.gto.nyql.model.units.AParam
/**
 * @author IWEERARATHNA
 */
trait ScriptTraits {

    abstract $IMPORT(String scriptId)

    abstract AParam PARAM(String name, AParam.ParamScope scope, String mappingName)

    abstract AParam PARAMLIST(String name)

    abstract AParam PARAM_DATE(String name)

    abstract AParam PARAM_TIMESTAMP(String name, String format)
}