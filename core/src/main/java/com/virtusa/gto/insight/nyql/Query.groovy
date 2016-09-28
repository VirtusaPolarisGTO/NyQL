package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.exceptions.NySyntaxException
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.traits.DataTypeTraits
import com.virtusa.gto.insight.nyql.traits.FunctionTraits
import com.virtusa.gto.insight.nyql.traits.ScriptTraits
import com.virtusa.gto.insight.nyql.utils.Constants
import com.virtusa.gto.insight.nyql.utils.QueryType

import java.sql.JDBCType

/**
 * @author Isuru Weerarathna
 */
class Query implements FunctionTraits, DataTypeTraits, ScriptTraits {

    QContext _ctx

    Where whereObj = null
    Table sourceTbl = null
    def _limit

    Query(QContext contextParam) {
        _ctx = contextParam
    }

    def IMPORT(String scriptId) {
        return $IMPORT(scriptId)
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

    def LIMIT(Object total) {
        _limit = total
        return this
    }

    AParam PARAM(String name, JDBCType type=null, AParam.ParamScope scope=null, String mappingName) {
        return _ctx.addParam(new AParam(__name: name, type: type, scope: scope, __mappingParamName: mappingName))
    }

    AParam PARAM(String name, int length) {
        return _ctx.addParam(new AParam(__name: name, length: length))
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

    def WHERE(closure) {
        Where whr = new Where(_ctx)

        def code = closure.rehydrate(whr, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        whereObj = whr
        return this
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

    def COLUMN(String colName) {
        if (_ctx.columns.containsKey(colName)) {
            return _ctx.columns.get(colName)
        } else {
            throw new Exception("No column is found by name '$colName'!")
        }
    }

    def propertyMissing(String name) {
        if (name == Constants.DSL_SESSION_WORD) {
            return _ctx.ownerSession.sessionVariables
        }

        Column col = _ctx.getColumnIfExist(name)
        if (col != null) {
            return col
        }

        if (_ctx.tables.containsKey(name)) {
            return _ctx.tables[name]
        } else if (Character.isUpperCase(name.charAt(0))) {
            // table name
            Table table = new Table(__name: name, _ctx: _ctx)
            _ctx.tables.put(name, table)
            return table
        } else {
            def column = _ctx.getTheOnlyTable()?.COLUMN(name)
            if (column != null) {
                return column
            }
            throw new Exception("No table by name '$name' found!")
        }
    }

    def methodMissing(String name, def args) {
        if (name == '$IMPORT') {
            return this.invokeMethod(name, args)
            //return
        }

        try {
            return _ctx.translator.invokeMethod(name, args)
        } catch (Exception ignored) {
            throw new Exception("Unsupported function for $_ctx.translatorName is found! (Function: '$name')")
        }
    }

}