package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.exceptions.NySyntaxException
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.blocks.AParam
import com.virtusa.gto.insight.nyql.model.blocks.ParamList
import com.virtusa.gto.insight.nyql.traits.DataTypeTraits
import com.virtusa.gto.insight.nyql.traits.FunctionTraits
import com.virtusa.gto.insight.nyql.traits.ScriptTraits
import com.virtusa.gto.insight.nyql.utils.Constants
import com.virtusa.gto.insight.nyql.utils.QUtils
import com.virtusa.gto.insight.nyql.utils.QueryType

import java.sql.JDBCType

/**
 * @author Isuru Weerarathna
 */
class Where implements DataTypeTraits, FunctionTraits, ScriptTraits {

    QContext _ctx = null

    List<Object> clauses = new ArrayList<>()

    public Where(QContext context) {
        _ctx = context
    }

    def ALL(@DelegatesTo(Where) closure) {
        Where inner = new Where(_ctx)
        def code = closure.rehydrate(inner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        clauses.add(new QConditionGroup(where: inner, condConnector: "AND"))
        return this
    }

    def ANY(closure) {
        Where inner = new Where(_ctx)
        def code = closure.rehydrate(inner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        clauses.add(new QConditionGroup(where: inner, condConnector: "OR"))
        return this
    }

    def LIKE(Object c1, Object c2) {
        return ON(c1, LIKE(c2))
    }

    def NOTLIKE(Object c1, Object c2) {
        return ON(c1, NOTLIKE(c2))
    }

    AParam PARAM(String name, JDBCType type=null, AParam.ParamScope scope=null, String mappingName=null) {
        return _ctx.addParam(new AParam(__name: name, type: type))
    }

    AParam PARAMLIST(String name) {
        return _ctx.addParam(new ParamList(__name: name))
    }

    def AND(Closure closure) {
        AND()

        def code = closure.rehydrate(this, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        return this
    }

    def OR(Closure closure) {
        OR()

        def code = closure.rehydrate(this, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        return this
    }

    def OR() {
        clauses.add(" OR ")
        return this
    }

    def AND() {
        clauses.add(" AND ")
        return this
    }

    def ON(Object c1, String op="", Object c2) {
        clauses.add(new QCondition(leftOp: c1, rightOp: c2, op: op))
        return this
    }

    def ISNULL(Object c) {
        //String str = _ctx.translator.___resolve(c, QContextType.CONDITIONAL) + " " + _ctx.translator.COMPARATOR_NULL() + " " + _ctx.translator.NULL()
        //clauses.add(str)
        clauses.add(new QCondition(leftOp: c, rightOp: null, op: "IS"))
        return this
    }

    def NOTNULL(Object c) {
        //String str = _ctx.translator.___resolve(c, QContextType.CONDITIONAL) + " " + _ctx.translator.COMPARATOR_NULL() + " NOT " + _ctx.translator.NULL()
        //clauses.add(str)
        clauses.add(new QCondition(leftOp: c, rightOp: null, op: "IS NOT"))
        return this
    }

    def EQ(Object c1, Object c2) {
        if (c2 == null) {
            return ISNULL(c1)
        }
        return ON(c1, "=", c2)
    }

    def NEQ(Object c1, Object c2) {
        if (c2 == null) {
            return NOTNULL(c1)
        }
        return ON(c1, "<>", c2)
    }

    def GT(Object c1, Object c2) {
        return ON(c1, ">", c2)
    }

    def GTE(Object c1, Object c2) {
        return ON(c1, ">=", c2)
    }

    def LT(Object c1, Object c2) {
        return ON(c1, "<", c2)
    }

    def LTE(Object c1, Object c2) {
        return ON(c1, "<=", c2)
    }

    def BETWEEN(Object c1, Object startValue, Object endValue) {
        return ON(c1, BETWEEN(startValue, endValue))
    }

    def NOTBETWEEN(Object c1, Object startValue, Object endValue) {
        return ON(c1, NOT_BETWEEN(startValue, endValue))
    }

    def IN(Object c1, Object... cs) {
        if (cs != null) {
            List list = new LinkedList()
            QUtils.expandToList(list, cs)
            if (list.size() == 0) {
                list.add(null)
            }
            return ON(c1, _ctx.translator.OP_IN(), list)
        }
    }

    def NOTIN(Object c1, Object... cs) {
        if (cs != null) {
            List list = new LinkedList()
            QUtils.expandToList(list, cs)
            if (list.size() == 0) {
                list.add(null)
            }
            return ON(c1, _ctx.translator.OP_NOTIN(), list)
        }
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

    def RAW(String cla) {
        clauses.add(cla)
    }

    def __hasClauses() {
        return QUtils.notNullNorEmpty(clauses)
    }

    def propertyMissing(String name) {
        if ("AND" == name) {
            return AND()
        } else if ("OR" == name) {
            return OR()
        } else if (name == Constants.DSL_SESSION_WORD) {
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
            def column = _ctx.getTheOnlyTable()?.COLUMN(name)
            if (column != null) {
                return column
            }
            throw new Exception("No table by name '$name' found!")
        }
    }

    def methodMissing(String name, def args) {
        if (name == "AND" || name == "OR") {
            if (args.getClass().isArray() && args[0] instanceof Where) {
                ((Where) args[0]).appendOneLastBefore(" " + name + " ");
                return
            }
        }
        throw new NySyntaxException("Unknown function detected! [Name: '$name', params: $args]")
    }

    private void appendOneLastBefore(Object clause) {
        if (clauses.size() > 0) {
            int idx = clauses.size() - 1
            clauses.add(idx, clause)
        } else {
            clauses.add(clause)
        }
    }

    static class QConditionGroup {
        Where where
        String condConnector = "AND"
    }

    static class QCondition {
        def leftOp
        def rightOp
        def op
    }
}
