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
import com.virtusa.gto.nyql.utils.Constants
import com.virtusa.gto.nyql.utils.QOperator
import com.virtusa.gto.nyql.utils.QUtils
import com.virtusa.gto.nyql.utils.QueryType
import groovy.transform.CompileStatic
/**
 * @author Isuru Weerarathna
 */
class Where implements DataTypeTraits, FunctionTraits, ScriptTraits {

    private static final String STR_AND = 'AND'
    private static final String STR_OR = 'OR'

    QContext _ctx = null

    List<Object> clauses = new ArrayList<>()

    Where(QContext context) {
        _ctx = context
    }

    @CompileStatic
    Where ALL(@DelegatesTo(value = Where, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        Where inner = new Where(_ctx)
        def code = closure.rehydrate(inner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        clauses.add(new QConditionGroup(where: inner, condConnector: QOperator.AND))
        this
    }

    @CompileStatic
    Where ANY(@DelegatesTo(value = Where, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        Where inner = new Where(_ctx)
        def code = closure.rehydrate(inner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        clauses.add(new QConditionGroup(where: inner, condConnector: QOperator.OR))
        this
    }

    @CompileStatic
    Where LIKE(Object c1, Object c2) {
        ON(c1, LIKE(c2))
    }

    @CompileStatic
    Where NOTLIKE(Object c1, Object c2) {
        ON(c1, NOTLIKE(c2))
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
    Where AND(@DelegatesTo(value = Where, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        AND()

        def code = closure.rehydrate(this, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        this
    }

    @CompileStatic
    Where OR(@DelegatesTo(value = Where, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        OR()

        def code = closure.rehydrate(this, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        this
    }

    @CompileStatic
    Where OR() {
        clauses.add(QOperator.OR)
        this
    }

    @CompileStatic
    Where AND() {
        clauses.add(QOperator.AND)
        this
    }

    @CompileStatic
    Where ON(Object c1, QOperator op = QOperator.UNKNOWN, Object c2) {
        clauses.add(new QCondition(leftOp: c1, rightOp: c2, op: op))
        this
    }

    @CompileStatic
    Where ISNULL(Object c) {
        clauses.add(new QCondition(leftOp: c, rightOp: null, op: QOperator.IS))
        this
    }

    @CompileStatic
    Where NOTNULL(Object c) {
        clauses.add(new QCondition(leftOp: c, rightOp: null, op: QOperator.IS_NOT))
        this
    }

    @CompileStatic
    Where EQ(Object c1, Object c2) {
        if (c2 == null) {
            return ISNULL(c1)
        }
        ON(c1, QOperator.EQUAL, c2)
    }

    @CompileStatic
    Where NEQ(Object c1, Object c2) {
        if (c2 == null) {
            return NOTNULL(c1)
        }
        ON(c1, QOperator.NOT_EQUAL, c2)
    }

    @CompileStatic
    Where GT(Object c1, Object c2) {
        ON(c1, QOperator.GREATER_THAN, c2)
    }

    @CompileStatic
    Where GTE(Object c1, Object c2) {
        ON(c1, QOperator.GREATER_THAN_EQUAL, c2)
    }

    @CompileStatic
    Where LT(Object c1, Object c2) {
        ON(c1, QOperator.LESS_THAN, c2)
    }

    @CompileStatic
    Where LTE(Object c1, Object c2) {
        ON(c1, QOperator.LESS_THAN_EQUAL, c2)
    }

    @CompileStatic
    Where BETWEEN(Object c1, Object startValue, Object endValue) {
        ON(c1, BETWEEN(startValue, endValue))
    }

    @CompileStatic
    Where NOTBETWEEN(Object c1, Object startValue, Object endValue) {
        ON(c1, NOT_BETWEEN(startValue, endValue))
    }

    @CompileStatic
    Where IN(Object c1, Object... cs) {
        if (cs != null) {
            List list = new LinkedList()
            QUtils.expandToList(list, cs)
            if (list.size() == 0) {
                list.add(null)
            }
            ON(c1, QOperator.IN, list)
        }
        this
    }

    @CompileStatic
    Where NOTIN(Object c1, Object... cs) {
        if (cs != null) {
            List list = new LinkedList()
            QUtils.expandToList(list, cs)
            if (list.isEmpty()) {
                list.add(null)
            }
            ON(c1, QOperator.NOT_IN, list)
        }
        this
    }

    QResultProxy QUERY(@DelegatesTo(value = QuerySelect, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        QuerySelect querySelect = new QuerySelect(_ctx)

        def code = closure.rehydrate(querySelect, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        _ctx.translator.___selectQuery(querySelect)
    }

    def $IMPORT(String scriptId) {
        QScript script = _ctx.ownerSession.scriptRepo.parse(scriptId, _ctx.ownerSession)
        QResultProxy proxy = script.proxy
        if (proxy.queryType == QueryType.PART && proxy.rawObject instanceof Where) {
            Query q = proxy.qObject as Query
            Where inner = proxy.rawObject
            clauses.addAll(inner.clauses)
            _ctx.mergeFrom(q._ctx)
            return this
        } else {
            return proxy
        }
    }

    def $IMPORT_UNSAFE(String scriptId) {
        try {
            return this.$IMPORT(scriptId)
        } catch (NyScriptNotFoundException ignored) {
            return null
        }
    }

    @CompileStatic
    Case CASE(@DelegatesTo(value = Case, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        Case aCase = new Case(_ctx: _ctx, _ownerQ: _ctx.ownQuery)

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
    Where EXISTS(Object subQuery) {
        clauses.add(new QUnaryCondition(rightOp: subQuery, op: QOperator.EXISTS))
        this
    }

    @CompileStatic
    Where NOTEXISTS(Object subQuery) {
        clauses.add(new QUnaryCondition(rightOp: subQuery, op: QOperator.NOT_EXISTS))
        this
    }

    @CompileStatic
    Where RAW(Object val) {
        clauses.add(val)
        this
    }

    @CompileStatic
    boolean __hasClauses() {
        QUtils.notNullNorEmpty(clauses)
    }

    def propertyMissing(String name) {
        if (STR_AND == name) {
            return AND()
        } else if (STR_OR == name) {
            return OR()
        } else if (name == Constants.DSL_SESSION_WORD) {
            return _ctx.ownerSession.sessionVariables
        }

        if (_ctx.tables.containsKey(name)) {
            return _ctx.tables[name]
        } else {
            if (_ctx.columns.containsKey(name)) {
                return _ctx.columns.get(name)
            }

            def column = _ctx.getTheOnlyTable()?.COLUMN(name)
            if (column != null) {
                return column
            }
            throw new NySyntaxException("No table by name '$name' found!")
        }
    }

    def methodMissing(String name, def args) {
        if (name == STR_AND || name == STR_OR) {
            if (args.getClass().isArray() && args[0] instanceof Where) {
                ((Where) args[0]).appendOneLastBefore(name + ' ')
                return
            }
        }
        throw new NySyntaxException("Unknown function detected! [Name: '$name', params: $args]")
    }

    @CompileStatic
    protected void appendOneLastBefore(String clause) {
        if (clauses.size() > 0) {
            int idx = clauses.size() - 1
            clauses.add(idx, ' ' + clause)
        } else {
            clauses.add(clause)
        }
    }

    @CompileStatic
    static class QConditionGroup {
        Where where
        QOperator condConnector = QOperator.AND
    }

    @CompileStatic
    static class QCondition {
        def leftOp
        def rightOp
        QOperator op
    }

    @CompileStatic
    static class QUnaryCondition extends QCondition {
        def chooseOp() {
            return leftOp ?: rightOp
        }
    }

}
