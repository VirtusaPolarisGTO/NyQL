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
class Query extends AbstractClause {

    Where whereObj = null
    Table sourceTbl = null
    def _limit

    Query(QContext contextParam) {
        super(contextParam)
    }

    def IMPORT(String scriptId) {
        return $IMPORT(scriptId)
    }

    def LIMIT(Object total) {
        _limit = total
        return this
    }

    def WHERE(closure) {
        Where whr = new Where(_ctx)

        def code = closure.rehydrate(whr, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        whereObj = whr
        return this
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
        } else if (_ctx.tables.containsKey(name)) {
            return _ctx.tables[name]
        }

        try {
            return _ctx.translator.invokeMethod(name, args)
        } catch (Exception ignored) {
            throw new Exception("Unsupported function for $_ctx.translatorName is found! (Function: '$name')")
        }
    }

}