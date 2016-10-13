package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.exceptions.NySyntaxException
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.blocks.AParam
import com.virtusa.gto.insight.nyql.model.blocks.ParamList
import com.virtusa.gto.insight.nyql.traits.DataTypeTraits
import com.virtusa.gto.insight.nyql.traits.ScriptTraits
import com.virtusa.gto.insight.nyql.utils.Constants
import com.virtusa.gto.insight.nyql.utils.QUtils
import com.virtusa.gto.insight.nyql.utils.QueryType

/**
 * @author IWEERARATHNA
 */
class Assign implements DataTypeTraits, ScriptTraits {

    QContext _ctx = null
    Query pQuery

    List<Object> assignments = new ArrayList<>()

    public Assign(QContext context, Query parentQuery) {
        _ctx = context
        pQuery = parentQuery
    }

    AParam PARAM(String name, AParam.ParamScope scope=null, String mappingName=null) {
        return _ctx.addParam(QUtils.createParam(name, scope, mappingName))
    }

    AParam PARAMLIST(String name) {
        return _ctx.addParam(new ParamList(__name: name))
    }

    def CASE(closure) {
        Case aCase = new Case(_ctx: _ctx, _ownerQ: pQuery)

        def code = closure.rehydrate(aCase, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        return code()
    }

    def IFNULL(Column column, Object val) {
        Case aCase = CASE({
            WHEN { ISNULL(column) }
            THEN { val }
            ELSE { column }
        })
        aCase.setCaseType(Case.CaseType.IFNULL)
        return aCase
    }

    def IFNOTNULL(Column column, Object val) {
        return CASE({
            WHEN { NOTNULL(column) }
            THEN { val }
            ELSE { column }
        })
    }

    def EQ(Column c1, Object val) {
        assignments.add(new AnAssign(leftOp: c1, rightOp: val))
        return this
    }

    def SET_NULL(Column c1) {
        assignments.add(new AnAssign(leftOp: c1, rightOp: _ctx.translator.NULL()))
        return this
    }

    def __hasAssignments() {
        return QUtils.notNullNorEmpty(assignments)
    }

    def $IMPORT(String scriptId) {
        QScript script = _ctx.ownerSession.scriptRepo.parse(scriptId, _ctx.ownerSession)
        QResultProxy proxy = script.proxy
        if (proxy.queryType == QueryType.PART && proxy.rawObject instanceof Assign) {
            Assign inner = proxy.rawObject
            assignments.addAll(inner.assignments)
            Query q = proxy.qObject as Query
            _ctx.mergeFrom(q._ctx)
            return this
        } else {
            return proxy
        }
    }

    def propertyMissing(String name) {
        if (name == Constants.DSL_SESSION_WORD) {
            return _ctx.ownerSession.sessionVariables
        }

        if (_ctx.tables.containsKey(name)) {
            return _ctx.tables[name]
        } else if (Character.isUpperCase(name.charAt(0))) {
            // table name
            Table table = new Table(__name: name, _ctx: _ctx)
            _ctx.tables.put(name, table)
            return table
        } else {
            throw new NySyntaxException(QUtils.generateErrStr(
                    "No table by name '$name' found!",
                    "You cannot refer to a column without mentioning its table or alias.",
                    "Or, did you misspelled the table name?"
            ))
        }
    }

    static class AnAssign {
        def leftOp
        def rightOp
        String op = '='
    }
}
