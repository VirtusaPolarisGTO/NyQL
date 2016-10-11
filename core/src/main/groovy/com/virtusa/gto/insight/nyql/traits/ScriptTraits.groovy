package com.virtusa.gto.insight.nyql.traits

import com.virtusa.gto.insight.nyql.model.blocks.AParam

import java.sql.JDBCType

/**
 * @author IWEERARATHNA
 */
trait ScriptTraits {

    abstract def $IMPORT(String scriptId)

    abstract AParam PARAM(String name, AParam.ParamScope scope, String mappingName);

    abstract AParam PARAMLIST(String name);
}