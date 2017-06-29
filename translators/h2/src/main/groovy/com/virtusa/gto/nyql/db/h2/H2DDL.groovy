package com.virtusa.gto.nyql.db.h2

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
import groovy.transform.CompileStatic

import java.util.stream.Collectors

/**
 * @author iweerarathna
 */
class H2DDL implements QDdl {

    @CompileStatic
    @Override
    List<QResultProxy> ___createTable(DTable dTable) {
        StringBuilder query = new StringBuilder('CREATE ')
        if (dTable.temporary) query.append('LOCAL TEMPORARY ')
        query.append('TABLE ')
        if (dTable.ifNotExist) query.append('IF NOT EXISTS ')
        query.append(___ddlResolve(dTable))

        if (QUtils.notNullNorEmpty(dTable.fields)) {
            query.append('(\n\t')
            List<DField> fields = dTable.fields
            boolean added = false
            for (DField f : fields) {
                if (added) query.append(', \n\t')

                query.append(___ddlExpandField(f))
                added = true
            }

            query.append('\n)')
        }

        QResultProxy resultProxy = new QResultProxy(query: query.toString(), orderedParameters: [], queryType: QueryType.SCHEMA_CHANGE)
        def list = Arrays.asList(resultProxy)

        // @TODO queries required with indexes



        list
    }

    @Override
    List<QResultProxy> ___dropTable(DTable dTable) {
        StringBuilder query = new StringBuilder('DROP ')
        query.append('TABLE ')
        if (dTable.ifNotExist) query.append("IF EXISTS ")
        query.append(___ddlResolve(dTable))
        def rProxy = new QResultProxy(query: query.toString(), orderedParameters: [], queryType: QueryType.SCHEMA_CHANGE)
        Arrays.asList(rProxy)
    }

    @Override
    def ___ddlResolve(Object obj) {
        if (obj instanceof DTable) {
            tblName(obj)
        } else if (obj instanceof DField) {
            colName(obj)
        } else {
            String.valueOf(obj)
        }
    }

    private static String ___ddlExpandKey(DKey key, DTable dTable) {
        if (key.type == DKeyType.PRIMARY) {
            return 'PRIMARY KEY ' + key.fields.stream().map { QUtils.quote(it, H2.BACK_TICK) }
                    .collect(Collectors.joining(', ', '(', ')'))
        } else if (key.type == DKeyType.INDEX) {
            return 'KEY ' + QUtils.quote(key.name, H2.BACK_TICK) + ' ' +
                    key.fields.stream().map { QUtils.quote(it, H2.BACK_TICK) }.collect(Collectors.joining(', ', '(', ')')) +
                    (key.indexType != null ? ' USING ' + key.indexType.name() : '')
        } else if (key.type == DKeyType.FOREIGN) {
            return 'CONSTRAINT ' + QUtils.quote(key.name) + ' FOREIGN KEY ' +
                    key.fields.stream().map { QUtils.quote(it, H2.BACK_TICK) } .collect(Collectors.joining(', ', '(', ')')) +
                    ' REFERENCES ' + QUtils.quoteIfWS(key.refTable, H2.BACK_TICK) +
                    key.refFields.stream().map { QUtils.quote(it, H2.BACK_TICK) } .collect(Collectors.joining(', ', '(', ')')) +
                    (key.onUpdate != DReferenceOption.NO_ACTION ? ' ON UPDATE ' + key.onUpdate.name().replace('_', ' ') : '') +
                    (key.onDelete != DReferenceOption.NO_ACTION ? ' ON DELETE ' + key.onDelete.name().replace('_', ' ') : '')
        }
        throw new NyException("Unknown table key type! [${key.type}]")
    }

    @CompileStatic
    private static String _resolveType(DFieldType fieldType, DField field) {
        switch (fieldType) {
            case DFieldType.BOOLEAN:    return 'BOOLEAN'
            case DFieldType.BIGINT:     return 'BIGINT'
            case DFieldType.DOUBLE:     return 'DOUBLE'
            case DFieldType.BINARY:     return 'BLOB' + _chkLength(field)
            case DFieldType.CHAR:       return 'CHAR' + _chkLength(field)
            case DFieldType.DATE:       return 'DATE'
            case DFieldType.DATETIME:   return 'DATETIME'
            case DFieldType.FLOAT:      return 'FLOAT'
            case DFieldType.INT:        return 'INT'
            case DFieldType.SMALLINT:   return 'MEDIUMINT'
            case DFieldType.TEXT:       return field.length > 0 ? 'VARCHAR' + _chkLength(field) : 'TEXT'
            case DFieldType.TIMESTAMP:  return 'TIMESTAMP'
            case DFieldType.ENUM:
                return 'ENUM' + ((List<String>)field.additionalAttrs['ENUMS'])
                        .stream().collect(Collectors.joining(',', '(', ')'))
        }
        throw new NySyntaxException("Unknown data type in field '${field.name}'! [Type: ${fieldType}]")
    }

    @CompileStatic
    private static String _chkLength(DField field) {
        if (field.length > 0) {
            return '(' + String.valueOf(field.length) + ')'
        }
        ''
    }

    @CompileStatic
    private static String ___ddlExpandField(DField dField) {
        StringBuilder q = new StringBuilder()
        q.append(colName(dField)).append(' ').append(_resolveType(dField.type, dField)).append(' ')

        if (dField.notNull) {
            q.append('NOT NULL ')
        }
        if (dField.sequence) {
            q.append('AUTO_INCREMENT ')
        }
        if (dField.specifiedDefault) {
            q.append('DEFAULT ').append(_describeDefaultVal(dField)).append(' ')
        }

        q.toString().trim()
    }

    @CompileStatic
    private static String _describeDefaultVal(DField dField) {
        if (!dField.specifiedDefault) { return '' }

        if (dField.defaultValue == null) {
            return 'NULL'
        }
        if (dField.type == DFieldType.DATETIME || dField.type == DFieldType.TIMESTAMP
                || dField.type == DFieldType.DATE) {
            if ('CURRENT_TIMESTAMP'.equalsIgnoreCase((String)dField.defaultValue)) {
                return 'CURRENT_TIMESTAMP'
            } else {
                return QUtils.quote((String)dField.defaultValue, "'")
            }
        }
        if (isNumber(dField.type)) {
            String.valueOf(dField.defaultValue)
        } else {
            QUtils.quote((String)dField.defaultValue, "'")
        }
    }

    @CompileStatic
    private static String tblName(DTable table) {
        QUtils.quote(table.name, H2.BACK_TICK)
    }

    @CompileStatic
    private static String colName(DField field) {
        QUtils.quote(field.name, H2.BACK_TICK)
    }

    @CompileStatic
    private static boolean isNumber(DFieldType type) {
        type == DFieldType.INT || type == DFieldType.BIGINT || type == DFieldType.DOUBLE ||
                type == DFieldType.FLOAT || type == DFieldType.NUMBER || type == DFieldType.SMALLINT
    }
}
