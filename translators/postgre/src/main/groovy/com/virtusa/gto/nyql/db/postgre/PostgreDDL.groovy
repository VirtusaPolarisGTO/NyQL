package com.virtusa.gto.nyql.db.postgre

import com.virtusa.gto.nyql.QResultProxy
import com.virtusa.gto.nyql.db.QDdl
import com.virtusa.gto.nyql.ddl.DField
import com.virtusa.gto.nyql.ddl.DFieldType
import com.virtusa.gto.nyql.ddl.DKey
import com.virtusa.gto.nyql.ddl.DKeyType
import com.virtusa.gto.nyql.ddl.DReferenceOption
import com.virtusa.gto.nyql.ddl.DTable
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.exceptions.NySyntaxException
import com.virtusa.gto.nyql.utils.QUtils
import com.virtusa.gto.nyql.utils.QueryType

import java.util.stream.Collectors

/**
 * @author IWEERARATHNA
 */
class PostgreDDL implements QDdl {

    @Override
    List<QResultProxy> ___createTable(DTable dTable) {
        List<QResultProxy> resultProxies = __checkSequences(dTable, true)
        resultProxies.addAll(__checkEnums(dTable, true))

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

                query.append(___ddlExpandField(dTable, f))
                added = true
            }

            query.append("\n)")
        }

        QResultProxy mainProxy = new QResultProxy(query: query.toString(), orderedParameters: [], queryType: QueryType.SCHEMA_CHANGE)
        resultProxies.add(mainProxy)

        // add indexes, keys, foreign keys
        resultProxies.addAll(___ddlExpandKeys(dTable))
        return resultProxies
    }

    @Override
    List<QResultProxy> ___dropTable(DTable dTable) {
        StringBuilder query = new StringBuilder("DROP ")
        // postgre does drop temp tables as normal tables.
        //if (dTable.temporary) query.append("TEMPORARY ")
        query.append("TABLE ")
        if (dTable.ifNotExist) query.append("IF EXISTS ")
        query.append(___ddlResolve(dTable))
        return Arrays.asList(new QResultProxy(query: query.toString(), orderedParameters: [], queryType: QueryType.SCHEMA_CHANGE))
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

    private static List<QResultProxy> ___ddlExpandKeys(DTable dTable) {
        List<QResultProxy> proxies = new LinkedList<>()

        if (QUtils.notNullNorEmpty(dTable.keys)) {
            for (DKey key : dTable.keys) {
                if (key.type == DKeyType.PRIMARY) {
                    String query = "ALTER TABLE ONLY " + tblName(dTable) +
                            " ADD CONSTRAINT " + tblName(dTable) + "_pkey PRIMARY KEY " +
                            key.fields.stream().map({ QUtils.quoteIfWS(it, Postgres.DOUBLE_QUOTE) }).collect(Collectors.joining(",", "(", ")"))
                    proxies.add(new QResultProxy(query: query, orderedParameters: [], queryType: QueryType.SCHEMA_CHANGE))
                } else if (key.type == DKeyType.INDEX) {
                    String query = "CREATE INDEX idx_" + QUtils.quote(dTable.name, Postgres.DOUBLE_QUOTE) +
                            key.fields.stream().collect(Collectors.joining("_")) + " ON " + dTable.name + " " +
                            (key.indexType != null ? " USING " + key.indexType.name().toLowerCase() + " " : "") +
                            key.fields.stream().map({ QUtils.quoteIfWS(it, Postgres.DOUBLE_QUOTE) }).collect(Collectors.joining(",", "(", ")"))
                    proxies.add(new QResultProxy(query: query, orderedParameters: [], queryType: QueryType.SCHEMA_CHANGE))
                } else if (key.type == DKeyType.FOREIGN) {
                    String query = "ALTER TABLE ONLY " + tblName(dTable) +
                            " ADD CONSTRAINT " + tblName(dTable) + "_" + key.fields.stream().collect(Collectors.joining("_")) + "_fkey" +
                            " FOREIGN KEY " + key.fields.stream().collect(Collectors.joining(",", "(", ")")) +
                            " REFERENCES " + QUtils.quoteIfWS(key.refTable, Postgres.DOUBLE_QUOTE) +
                            key.refFields.stream().map({ QUtils.quoteIfWS(it, Postgres.DOUBLE_QUOTE) }).collect(Collectors.joining(",", "(", ")")) +
                            (key.onUpdate != DReferenceOption.NO_ACTION ? " ON UPDATE " + key.onUpdate.name().replace('_', ' ') : "") +
                            (key.onDelete != DReferenceOption.NO_ACTION ? " ON DELETE " + key.onDelete.name().replace('_', ' ') : "")
                    proxies.add(new QResultProxy(query: query, orderedParameters: [], queryType: QueryType.SCHEMA_CHANGE))
                }
                throw new NyException("Unknown table key type! [${key.type}]")
            }
        }

        return proxies
    }

    private static List<QResultProxy> __checkEnums(DTable dTable, boolean forCreation) {
        List<QResultProxy> proxies = new LinkedList<>()
        for (DField dField : dTable.fields) {
            if (dField.type == DFieldType.ENUM) {
                if (forCreation) {
                    String query = "CREATE TYPE " + ___deriveEnumName(dTable, dField) + " AS ENUM " +
                            ((List<String>)dField.additionalAttrs["ENUMS"]).stream().map({ "'" + it + "'" }).collect(Collectors.joining(",", "(", ")"))
                    proxies.add(new QResultProxy(query: query, orderedParameters: [], queryType: QueryType.SCHEMA_CHANGE))
                } else {
                    String query = "DROP TYPE IF EXISTS " + ___deriveEnumName(dTable, dField)
                    proxies.add(new QResultProxy(query: query, orderedParameters: [], queryType: QueryType.SCHEMA_CHANGE))
                }
            }
        }
        return proxies
    }

    private static List<QResultProxy> __checkSequences(DTable dTable, boolean forCreation) {
        List<QResultProxy> proxies = new LinkedList<>()
        for (DField dField : dTable.fields) {
            if (dField.sequence) {
                if (forCreation) {
                    String query = "CREATE SEQUENCE " + ___deriveSeqName(dTable, dField) +
                            " INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1"
                    proxies.add(new QResultProxy(query: query, orderedParameters: [], queryType: QueryType.SCHEMA_CHANGE))
                } else {
                    String query = "DROP SEQUENCE IF EXISTS " + ___deriveSeqName(dTable, dField)
                    proxies.add(new QResultProxy(query: query, orderedParameters: [], queryType: QueryType.SCHEMA_CHANGE))
                }
            }
        }
        return proxies
    }

    private static String ___deriveSeqName(DTable dTable, DField dField) {
        return dTable.name + "_" + dField.name + "_id"
    }

    private static String ___deriveEnumName(DTable dTable, DField dField) {
        return dTable.name + "_" + dField.name + "_enum"
    }

    private static String ___ddlExpandField(DTable dTable, DField dField) {
        StringBuilder q = new StringBuilder()
        q.append(colName(dField)).append(" ").append(_resolveType(dTable, dField.type, dField)).append(" ")

        if (dField.sequence) {
            q.append("DEFAULT nextval('").append(___deriveSeqName(dTable, dField)).append("'::regclass) ")
        }
        if (dField.sequence || dField.notNull) {
            q.append("NOT NULL ")
        }
        if (!dField.sequence && dField.specifiedDefault) {
            q.append("DEFAULT ").append(_describeDefaultVal(dTable, dField)).append(" ")
        }

        return q.toString().trim()
    }

    private static String _resolveType(DTable dTable, DFieldType fieldType, DField field) {
        switch (fieldType) {
            case DFieldType.BOOLEAN:    return "boolean"
            case DFieldType.BIGINT:     return "bigint"
            case DFieldType.DOUBLE:     return "double precision"
            case DFieldType.BINARY:     return "bytea"
            case DFieldType.CHAR:       return "char" + _chkLength(field)
            case DFieldType.DATE:       return "date"
            case DFieldType.DATETIME:   return "timestamp"
            case DFieldType.FLOAT:      return "real"
            case DFieldType.INT:        return "integer"
            case DFieldType.SMALLINT:   return "smallint"
            case DFieldType.TEXT:       return field.length > 0 ? "varchar" + _chkLength(field) : "text"
            case DFieldType.TIMESTAMP:  return "timestamp with time zone"
            case DFieldType.ENUM:       return ___deriveEnumName(dTable, field)
        }
        throw new NySyntaxException("Unknown data type in field '${field.name}'! [Type: ${fieldType}]")
    }

    private static String _describeDefaultVal(DTable dTable, DField dField) {
        if (!dField.specifiedDefault) { return  "" }

        if (dField.defaultValue == null) {
            return "NULL"
        }

        if (dField.type == DFieldType.ENUM) {
            return "'" + dField.defaultValue + "'::" + ___deriveEnumName(dTable, dField)
        }

        if (dField.type == DFieldType.DATETIME || dField.type == DFieldType.TIMESTAMP
                || dField.type == DFieldType.DATE) {
            if ("CURRENT_TIMESTAMP".equalsIgnoreCase(dField.defaultValue)) {
                return "now()"
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

    private static String _chkLength(DField field) {
        if (field.length > 0) {
            return "(" + String.valueOf(field.length) + ")"
        }
        return ""
    }

    private static String tblName(DTable table) {
        return QUtils.quoteIfWS(table.name, Postgres.DOUBLE_QUOTE)
    }

    private static String colName(DField field) {
        return QUtils.quoteIfWS(field.name, Postgres.DOUBLE_QUOTE)
    }

    private static boolean isNumber(DFieldType type) {
        return type == DFieldType.INT || type == DFieldType.BIGINT || type == DFieldType.DOUBLE ||
                type == DFieldType.FLOAT || type == DFieldType.NUMBER || type == DFieldType.SMALLINT
    }
}
