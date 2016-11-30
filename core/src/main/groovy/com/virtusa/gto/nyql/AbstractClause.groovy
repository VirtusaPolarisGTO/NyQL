package com.virtusa.gto.nyql

import com.virtusa.gto.nyql.exceptions.NyScriptNotFoundException
import com.virtusa.gto.nyql.exceptions.NySyntaxException
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.units.AParam
import com.virtusa.gto.nyql.model.units.ParamDate
import com.virtusa.gto.nyql.model.units.ParamList
import com.virtusa.gto.nyql.model.units.ParamTimestamp
import com.virtusa.gto.nyql.traits.DataTypeTraits
import com.virtusa.gto.nyql.traits.FunctionTraits
import com.virtusa.gto.nyql.traits.ScriptTraits
import com.virtusa.gto.nyql.utils.QUtils
import com.virtusa.gto.nyql.utils.QueryType
import groovy.transform.CompileStatic
/**
 * @author IWEERARATHNA
 */
abstract class AbstractClause implements FunctionTraits, DataTypeTraits, ScriptTraits {

    QContext _ctx

    protected AbstractClause(QContext contextParam) {
        _ctx = contextParam
        if (this instanceof Query) {
            _ctx.ownQuery = this
        }
        set$SESSION(_ctx.ownerSession.sessionVariables)
    }

    @CompileStatic
    def $IMPORT(String scriptId) {
        QScript script = _ctx.ownerSession.scriptRepo.parse(scriptId, _ctx.ownerSession)
        def proxy = script.proxy
        if (proxy.queryType == QueryType.PART) {
            Query q = proxy.qObject as Query
            _ctx.mergeFrom(q._ctx)
            return proxy.rawObject
        } else {
            return script.proxy
        }
        //throw new NySyntaxException("You can only import a query part having a Table reference!")
    }

    @CompileStatic
    def $IMPORT_UNSAFE(String scriptId) {
        try {
            return this.$IMPORT(scriptId)
        } catch (NyScriptNotFoundException ignored) {
            return null
        }
    }

    @CompileStatic
    AParam PARAM(String name, AParam.ParamScope scope=null, String mappingName=null) {
        _ctx.addParam(QUtils.createParam(name, scope, mappingName))
    }

    @CompileStatic
    AParam PARAMLIST(String name) {
        _ctx.addParam(new ParamList(__name: name))
    }

    @CompileStatic
    @Override
    AParam PARAM_DATE(String name) {
        _ctx.addParam(new ParamDate(__name: name))
    }

    @CompileStatic
    @Override
    AParam PARAM_TIMESTAMP(String name, String format) {
        _ctx.addParam(new ParamTimestamp(__name: name, __tsFormat: format))
    }

    @CompileStatic
    AParam PARAM_TIMESTAMP(String name) {
        PARAM_TIMESTAMP(name, null)
    }

    @CompileStatic
    Table TABLE(String tblName) {
        if (_ctx.tables.containsKey(tblName)) {
            _ctx.tables.get(tblName)
        } else {
            Table table = new Table(__name: tblName, _ctx: _ctx)
            _ctx.tables.put(tblName, table)
            table
        }
    }

    @CompileStatic
    TableProxy OTHER() {
        TableProxy tableProxy = new TableProxy(__name: String.valueOf(System.currentTimeMillis()), _ctx: _ctx)
        _ctx.tables.putIfAbsent(tableProxy.__name, tableProxy)
        tableProxy
    }

    @CompileStatic
    Table TABLE(QResultProxy resultProxy) {
        Table table = new Table(__name: String.valueOf(System.currentTimeMillis()), _ctx: _ctx, __resultOf: resultProxy)
        _ctx.tables.putIfAbsent(table.__name, table)
        table
    }

    Table TABLE(QScript qScript) {
        TABLE(qScript.proxy)
    }

    Table QUERY(@DelegatesTo(value = QuerySelect, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        QuerySelect querySelect = new QuerySelect(_ctx)

        def code = closure.rehydrate(querySelect, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        return TABLE(_ctx.translator.___selectQuery(querySelect))
    }

    @CompileStatic
    Column COLUMN(String colName) {
        if (_ctx.columns.containsKey(colName)) {
            return _ctx.columns.get(colName)
        } else {
            def column = _ctx.getTheOnlyTable()?.COLUMN(colName)
            if (column != null) {
                return column
            }

            throw new NySyntaxException(QUtils.generateErrStr(
                    "No column is found by name '$colName'!",
                    'If you are specifying column name directly then make sure it has an alias!'
            ))
        }
    }

    @CompileStatic
    Column ALIAS_REF(String aliasStr) {
        Column column = _ctx.getColumnIfExist(aliasStr)
        if (column == null) {
            throw new NySyntaxException('No column is found by alias \'' + aliasStr + '\'!')
        }
        column
    }

    Case CASE(@DelegatesTo(value = Case, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        Case aCase = new Case(_ctx: _ctx, _ownerQ: this)

        def code = closure.rehydrate(aCase, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        aCase
    }

    Case IFNULL(Column column, Object val) {
        Case aCase = CASE {
            WHEN { ISNULL(column) }
            THEN { val }
            ELSE { column }
        }
        aCase.setCaseType(Case.CaseType.IFNULL)
        aCase
    }

    Case IFNOTNULL(Column column, Object val) {
        CASE {
            WHEN { NOTNULL(column) }
            THEN { val }
            ELSE { column }
        }
    }

    FunctionColumn EXISTS(QResultProxy innerQuery) {
        new FunctionColumn(_ctx: _ctx, _setOfCols: true, _func: 'exists', _columns: [innerQuery])
    }

    String ALL = '*'

    Map $SESSION

    def propertyMissing(String name) {
        //if (name == Constants.DSL_SESSION_WORD) {
        //    return _ctx.ownerSession.sessionVariables
        //}

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
            throw new NySyntaxException(QUtils.generateErrStr(
                    "No table by name '$name' found!",
                    'You cannot refer to a column without mentioning its table or alias.',
                    'Or, did you misspelled the table name?'
            ))
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
            throw new NySyntaxException("Unsupported function for $_ctx.translatorName is found! (Function: '$name')")
        }
    }
}
