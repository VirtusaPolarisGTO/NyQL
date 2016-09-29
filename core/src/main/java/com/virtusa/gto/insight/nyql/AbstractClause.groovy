package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.exceptions.NySyntaxException
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.traits.DataTypeTraits
import com.virtusa.gto.insight.nyql.traits.FunctionTraits
import com.virtusa.gto.insight.nyql.traits.ScriptTraits
import com.virtusa.gto.insight.nyql.utils.QueryType

import java.sql.JDBCType

/**
 * @author IWEERARATHNA
 */
abstract class AbstractClause implements FunctionTraits, DataTypeTraits, ScriptTraits {

    QContext _ctx

    protected AbstractClause(QContext contextParam) {
        _ctx = contextParam
    }

    def $IMPORT(String scriptId) {
        QScript script = _ctx.ownerSession.scriptRepo.parse(scriptId, _ctx.ownerSession)
        def proxy = script.proxy
        if (proxy.queryType == QueryType.PART) {
            Query q = proxy.qObject as Query
            _ctx.mergeFrom(q._ctx)
            return proxy.rawObject
        }
        throw new NySyntaxException("You can only import a query part having a Table reference!")
    }

    AParam PARAM(String name, JDBCType type=null, AParam.ParamScope scope=null, String mappingName=null) {
        return _ctx.addParam(new AParam(__name: name, type: type, scope: scope, __mappingParamName: mappingName))
    }

    AParam PARAM(String name, int length) {
        return _ctx.addParam(new AParam(__name: name, length: length))
    }

    def TABLE(String tblName) {
        if (_ctx.tables.containsKey(tblName)) {
            return _ctx.tables.get(tblName)
        } else {
            Table table = new Table(__name: tblName, _ctx: _ctx)
            _ctx.tables.put(tblName, table)
            return table
        }
    }

    def TABLE(QResultProxy resultProxy) {
        Table table = new Table(__name: String.valueOf(System.currentTimeMillis()), _ctx: _ctx, __resultOf: resultProxy)
        _ctx.tables.putIfAbsent(table.__name, table)
        return table
    }

    def TABLE(QScript qScript) {
        return TABLE(qScript.proxy)
    }

    def COLUMN(String colName) {
        if (_ctx.columns.containsKey(colName)) {
            return _ctx.columns.get(colName)
        } else {
            throw new Exception("No column is found by name '$colName'!")
        }
    }

    def CASE(closure) {
        Case aCase = new Case(_ctx: _ctx, _ownerQ: this)

        def code = closure.rehydrate(aCase, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        return code()
    }

    def IFNULL(Column column, Object val) {
        return CASE({
            WHEN { ISNULL(column) }
            THEN { val }
            ELSE { column }
        })
    }

    def IFNOTNULL(Column column, Object val) {
        return CASE({
            WHEN { NOTNULL(column) }
            THEN { val }
            ELSE { column }
        })
    }
}
