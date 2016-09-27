package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.traits.DataTypeTraits
import com.virtusa.gto.insight.nyql.traits.ScriptTraits
import com.virtusa.gto.insight.nyql.utils.Constants
import com.virtusa.gto.insight.nyql.utils.QUtils
import com.virtusa.gto.insight.nyql.utils.QueryType

import java.sql.JDBCType

/**
 * @author IWEERARATHNA
 */
class Assign implements DataTypeTraits, ScriptTraits {

    QContext _ctx = null

    List<Object> assignments = new ArrayList<>()

    public Assign(QContext context) {
        _ctx = context
    }

    AParam PARAM(String name, JDBCType type=null, AParam.ParamScope scope=null, String mappingName=null) {
        return _ctx.addParam(new AParam(__name: name, type: type, scope: scope, __mappingParamName: mappingName))
    }

    AParam PARAM(String name, int length) {
        return _ctx.addParam(new AParam(__name: name, length: length))
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
            throw new Exception("No table by name $name found!")
        }
    }

    static class AnAssign {
        def leftOp
        def rightOp
        String op = "="
    }
}
