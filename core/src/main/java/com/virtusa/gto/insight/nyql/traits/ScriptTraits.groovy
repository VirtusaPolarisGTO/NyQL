package com.virtusa.gto.insight.nyql.traits

import com.virtusa.gto.insight.nyql.AParam

import java.sql.JDBCType

/**
 * @author IWEERARATHNA
 */
trait ScriptTraits {

    abstract def $IMPORT(String scriptId)

    abstract AParam PARAM(String name, JDBCType type, AParam.ParamScope scope, String mappingName);

    abstract AParam PARAM(String name, int length);
}