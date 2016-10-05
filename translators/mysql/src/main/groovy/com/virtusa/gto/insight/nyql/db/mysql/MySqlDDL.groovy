package com.virtusa.gto.insight.nyql.db.mysql

import com.virtusa.gto.insight.nyql.QResultProxy
import com.virtusa.gto.insight.nyql.db.QDdl
import com.virtusa.gto.insight.nyql.ddl.DField
import com.virtusa.gto.insight.nyql.ddl.DFieldType
import com.virtusa.gto.insight.nyql.ddl.DKey
import com.virtusa.gto.insight.nyql.ddl.DKeyType
import com.virtusa.gto.insight.nyql.ddl.DReferenceOption
import com.virtusa.gto.insight.nyql.ddl.DTable
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.exceptions.NySyntaxException
import com.virtusa.gto.insight.nyql.utils.QUtils
import com.virtusa.gto.insight.nyql.utils.QueryType

import java.util.stream.Collectors

/**
 * @author IWEERARATHNA
 */
class MySqlDDL implements QDdl {

    @Override
    List<QResultProxy> ___createTable(DTable dTable) {
        StringBuilder query = new StringBuilder("CREATE ")
        if (dTable.temporary) query.append("TEMPORARY ")
        query.append("TABLE ")
        if (dTable.ifNotExist) query.append("IF NOT EXISTS ")
        query.append(___ddlResolve(dTable))

        if (QUtils.notNullNorEmpty(dTable.fields)) {
            query.append("(\n\t")
            List<DField> fields = dTable.fields
            boolean added = false
            for (DField f : fields) {
                if (added) query.append(",\n\t")

                query.append(___ddlExpandField(f))
                added = true
            }

            if (QUtils.notNullNorEmpty(dTable.keys)) {
                List<DKey> keys = dTable.keys
                for (DKey k : keys) {
                    query.append(",\n\t").append(___ddlExpandKey(k))
                }
            }

            query.append("\n)")
        }

        QResultProxy resultProxy = new QResultProxy(query: query.toString(), orderedParameters: [], queryType: QueryType.SCHEMA_CHANGE)
        return Arrays.asList(resultProxy)
    }

    @Override
    List<QResultProxy> ___dropTable(DTable dTable) {
        StringBuilder query = new StringBuilder("DROP ")
        if (dTable.temporary) query.append("TEMPORARY ")
        query.append("TABLE ")
        //query.append("IF EXISTS ")
        query.append(___ddlResolve(dTable))
        def rProxy = new QResultProxy(query: query.toString(), orderedParameters: [], queryType: QueryType.SCHEMA_CHANGE)
        return Arrays.asList(rProxy)
    }

    private static String ___ddlExpandKey(DKey key) {
        if (key.type == DKeyType.PRIMARY) {
            return "PRIMARY KEY " + key.fields.stream().map({ QUtils.quote(it, MySql.BACK_TICK) })
                    .collect(Collectors.joining(",", "(", ")"))
        } else if (key.type == DKeyType.INDEX) {
            return "KEY " + QUtils.quote(key.name, MySql.BACK_TICK) + " " +
                    key.fields.stream().map({ QUtils.quote(it, MySql.BACK_TICK) }).collect(Collectors.joining(",", "(", ")")) +
                    (key.indexType != null ? " USING " + key.indexType.name() : "")
        } else if (key.type == DKeyType.FOREIGN) {
            return "CONSTRAINT " + QUtils.quote(key.name) + " FOREIGN KEY " +
                    key.fields.stream().map({ QUtils.quote(it, MySql.BACK_TICK) }).collect(Collectors.joining(",", "(", ")")) +
                    " REFERENCES " + QUtils.quoteIfWS(key.refTable, MySql.BACK_TICK) +
                    key.refFields.stream().map({ QUtils.quote(it, MySql.BACK_TICK) }).collect(Collectors.joining(",", "(", ")")) +
                    (key.onUpdate != DReferenceOption.NO_ACTION ? " ON UPDATE " + key.onUpdate.name().replace('_', ' ') : "") +
                    (key.onDelete != DReferenceOption.NO_ACTION ? " ON DELETE " + key.onDelete.name().replace('_', ' ') : "")
        }
        throw new NyException("Unknown table key type! [${key.type}]")
    }

    private static String ___ddlExpandField(DField dField) {
        StringBuilder q = new StringBuilder()
        q.append(colName(dField)).append(" ").append(_resolveType(dField.type, dField)).append(" ")

        if (dField.notNull) {
            q.append("NOT NULL ")
        }
        if (dField.sequence) {
            q.append("AUTO_INCREMENT ")
        }
        if (dField.specifiedDefault) {
            q.append("DEFAULT ").append(_describeDefaultVal(dField)).append(" ")
        }

        return q.toString().trim()
    }

    @Override
    def ___ddlResolve(Object obj) {
        if (obj instanceof DTable) {
            return tblName(obj)
        } else if (obj instanceof DField) {
            return colName(obj)
        } else {
            return String.valueOf(obj)
        }
    }

    private static String _describeDefaultVal(DField dField) {
        if (!dField.specifiedDefault) { return  "" }

        if (dField.defaultValue == null) {
            return "NULL"
        }
        if (dField.type == DFieldType.DATETIME || dField.type == DFieldType.TIMESTAMP
            || dField.type == DFieldType.DATE) {
            if ("CURRENT_TIMESTAMP".equalsIgnoreCase(dField.defaultValue)) {
                return "CURRENT_TIMESTAMP"
            } else {
                return QUtils.quote(dField.defaultValue, "'")
            }
        }
        if (isNumber(dField.type)) {
            return String.valueOf(dField.defaultValue)
        } else {
            return QUtils.quote(dField.defaultValue, "'")
        }
    }

    private static String _resolveType(DFieldType fieldType, DField field) {
        switch (fieldType) {
            case DFieldType.BOOLEAN:    return "TINYINT(1)"
            case DFieldType.BIGINT:     return "BIGINT" + _chkLength(field) + _chkUnsigned(field)
            case DFieldType.DOUBLE:     return "DOUBLE" + _chkUnsigned(field)
            case DFieldType.BINARY:     return "BLOB"
            case DFieldType.CHAR:       return "CHAR" + _chkLength(field)
            case DFieldType.DATE:       return "DATE"
            case DFieldType.DATETIME:   return "DATETIME"
            case DFieldType.FLOAT:      return "FLOAT" + _chkUnsigned(field)
            case DFieldType.INT:        return "INT" + _chkLength(field) + _chkUnsigned(field)
            case DFieldType.SMALLINT:   return "SMALLINT" + _chkLength(field) + _chkUnsigned(field)
            case DFieldType.TEXT:       return field.length > 0 ? "VARCHAR" + _chkLength(field) : "TEXT"
            case DFieldType.TIMESTAMP:  return "TIMESTAMP"
            case DFieldType.ENUM:
                return "ENUM" + ((List<String>)field.additionalAttrs["ENUMS"])
                                    .stream().collect(Collectors.joining(",", "(", ")"))
        }
        throw new NySyntaxException("Unknown data type in field '${field.name}'! [Type: ${fieldType}]")
    }

    private static String _chkUnsigned(DField field) {
        return field.unsigned ? " UNSIGNED" : ""
    }

    private static String _chkLength(DField field) {
        if (field.length > 0) {
            return "(" + String.valueOf(field.length) + ")"
        }
        return ""
    }

    private static String tblName(DTable table) {
        return QUtils.quote(table.name, MySql.BACK_TICK)
    }

    private static String colName(DField field) {
        return QUtils.quote(field.name, MySql.BACK_TICK)
    }

    private static boolean isNumber(DFieldType type) {
        return type == DFieldType.INT || type == DFieldType.BIGINT || type == DFieldType.DOUBLE ||
                type == DFieldType.FLOAT || type == DFieldType.NUMBER || type == DFieldType.SMALLINT
    }
}
