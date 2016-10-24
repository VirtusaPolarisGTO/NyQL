package com.virtusa.gto.insight.nyql.ddl

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * @author IWEERARATHNA
 */
@CompileStatic
@ToString
class DTable extends DAbstractEntity {

    List<DField> fields = []
    List<DKey> keys = []

    boolean temporary = false
    boolean ifNotExist = false

    DTable FIELD(String name, DFieldType type, Map details=null) {
        DField dField = DField.parseFrom(details)
        dField.type = type
        dField.name = name
        fields.add(dField)
        this
    }

    DTable PRIMARY_KEY(String fieldName, Map details=null) {
        DKey dKey = DKey.parseFrom(details)
        dKey.fields = [fieldName]
        dKey.type = DKeyType.PRIMARY
        keys.add(dKey)
        this
    }

    DTable PRIMARY_KEY(List<String> fieldNames, Map details=null) {
        DKey dKey = DKey.parseFrom(details)
        dKey.fields = fieldNames
        dKey.type = DKeyType.PRIMARY
        keys.add(dKey)
        this
    }

    DTable PRIMARY_KEY(String... fieldNames) {
        DKey dKey = new DKey()

        List<String> items = new LinkedList<>()
        fieldNames.each { items.add((String)it) }
        dKey.fields = items
        dKey.type = DKeyType.PRIMARY
        keys.add(dKey)
        this
    }

    DTable INDEX(String name, Map details=null) {
        DKey dKey = DKey.parseFrom(details)
        dKey.type = DKeyType.INDEX
        dKey.name = name
        keys.add(dKey)
        this
    }

    DTable FOREIGN_KEY(String name, String thisTableField, Map details=null) {
        DKey dKey = DKey.parseFrom(details)
        dKey.type = DKeyType.FOREIGN
        dKey.fields = [thisTableField]
        dKey.name = name
        keys.add(dKey)
        this
    }

}
