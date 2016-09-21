package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.traits.DataTypeTraits
import com.virtusa.gto.insight.nyql.traits.FunctionTraits
import com.virtusa.gto.insight.nyql.traits.ScriptTraits
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
        inner = code()
        clauses.add(new QConditionGroup(where: inner, condConnector: "AND"))
        return this
    }

    def ANY(closure) {
        Where inner = new Where(_ctx)
        def code = closure.rehydrate(inner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        inner = code()
        clauses.add(new QConditionGroup(where: inner, condConnector: "OR"))
        return this
    }

    def P(String name, JDBCType type=null) {
        return _ctx.addParam(new AParam(__name: name, type: type))
    }

    def AND(Column c1, String op="", Column c2) {
        clauses.add(" AND ")
        ON(c1, op, c2)
        return this
    }

    def OR(Column c1, String op="", Column c2) {
        clauses.add(" OR ")
        ON(c1, op, c2)
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

    def ON(Column c1, String op="", Column c2) {
        clauses.add(new QCondition(leftOp: c1, rightOp: c2, op: op))
        return this
    }

    def ON(String c1, String op="", String c2) {
        clauses.add(new QCondition(leftOp: c1, rightOp: c2, op: op))
        return this
    }

    def ON(Column c1, String op="", Object c2) {
        clauses.add(new QCondition(leftOp: c1, rightOp: c2, op: op))
        return this
    }

    def ISNULL(Object c) {
        String str = _ctx.translator.___resolve(c, QContextType.CONDITIONAL) + " " + _ctx.translator.COMPARATOR_NULL + " " + _ctx.translator.NULL
        clauses.add(str)
        return this
    }

    def NOT_NULL(Object c) {
        String str = _ctx.translator.___resolve(c, QContextType.CONDITIONAL) + " " + _ctx.translator.COMPARATOR_NULL + " NOT " + _ctx.translator.NULL
        clauses.add(str)
        return this
    }

    def EQ(Column c1, Column c2) {
        return ON(c1, "=", c2)
    }

    def EQ(String c1, String c2) {
        clauses.add(new QCondition(leftOp: c1, rightOp: c2, op: "="))
        return this
    }

    def EQ(Column c1, Object c2) {
        clauses.add(new QCondition(leftOp: c1, rightOp: c2, op: "="))
        return this
    }

    def EQ(Column c1, String s2) {
        clauses.add(new QCondition(leftOp: c1, rightOp: s2, op: "="))
        return this
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
        if (_ctx.tables.containsKey(name)) {
            return _ctx.tables[name]
        } else if (Character.isUpperCase(name.charAt(0))) {
            // table name
            Table table = new Table(__name: name, _ctx: _ctx)
            _ctx.tables.put(name, table)
            return table
        } else {
            throw new Exception("No table by name '$name' found!")
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
