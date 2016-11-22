package com.virtusa.gto.nyql

import com.virtusa.gto.nyql.exceptions.NyScriptNotFoundException
import com.virtusa.gto.nyql.exceptions.NySyntaxException
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.units.AParam
import com.virtusa.gto.nyql.model.units.ParamDate
import com.virtusa.gto.nyql.model.units.ParamTimestamp
import com.virtusa.gto.nyql.traits.DataTypeTraits
import com.virtusa.gto.nyql.traits.FunctionTraits
import com.virtusa.gto.nyql.traits.ScriptTraits
import com.virtusa.gto.nyql.utils.Constants
import com.virtusa.gto.nyql.utils.QOperator
import com.virtusa.gto.nyql.utils.QUtils
import com.virtusa.gto.nyql.utils.QueryType
import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
class Assign implements DataTypeTraits, FunctionTraits, ScriptTraits {

    QContext _ctx = null
    Query pQuery

    List<Object> assignments = new ArrayList<>()

    public Assign(QContext context, Query parentQuery) {
        _ctx = context
        pQuery = parentQuery
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

    @CompileStatic
    AParam PARAM(String name, AParam.ParamScope scope=null, String mappingName=null) {
        _ctx.addParam(QUtils.createParam(name, scope, mappingName))
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

    AParam PARAMLIST(String name) {
        throw new NySyntaxException('You cannot use list parameters when assigning!')
    }

    @CompileStatic
    QResultProxy QUERY(@DelegatesTo(value = QuerySelect, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        QuerySelect querySelect = new QuerySelect(_ctx)

        def code = closure.rehydrate(querySelect, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        _ctx.translator.___selectQuery(querySelect)
    }

    @CompileStatic
    Case CASE(@DelegatesTo(value = Case, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        Case aCase = new Case(_ctx: _ctx, _ownerQ: pQuery)

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

    @CompileStatic
    Assign EQ(Column c1, Object val) {
        assignments.add(new AnAssign(leftOp: c1, rightOp: val))
        this
    }

    @CompileStatic
    Assign SET_NULL(Column c1) {
        assignments.add(new AnAssign(leftOp: c1, rightOp: null))
        this
    }

    @CompileStatic
    boolean __hasAssignments() {
        QUtils.notNullNorEmpty(assignments)
    }

    def $IMPORT(String scriptId) {
        QScript script = _ctx.ownerSession.scriptRepo.parse(scriptId, _ctx.ownerSession)
        QResultProxy proxy = script.proxy
        if (proxy.queryType == QueryType.PART && proxy.rawObject instanceof Assign) {
            Assign inner = proxy.rawObject
            assignments.addAll(inner.assignments)
            Query q = proxy.qObject as Query
            _ctx.mergeFrom(q._ctx)
            this
        } else {
            proxy
        }
    }

    def $IMPORT_UNSAFE(String scriptId) {
        try {
            return this.$IMPORT(scriptId)
        } catch (NyScriptNotFoundException ignored) {
            return null
        }
    }

    def propertyMissing(String name) {
        if (name == Constants.DSL_SESSION_WORD) {
            return _ctx.ownerSession.sessionVariables
        }

        if (_ctx.tables.containsKey(name)) {
            return _ctx.tables[name]
        } else {
            throw new NySyntaxException(QUtils.generateErrStr(
                    "No table by name '$name' found!",
                    'You cannot refer to a column without mentioning its table or alias.',
                    'Or, did you misspelled the table name?'
            ))
        }
    }

    @CompileStatic
    static class AnAssign {
        def leftOp
        def rightOp
        QOperator op = QOperator.EQUAL
    }
}
