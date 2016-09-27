package com.virtusa.gto.insight.nyql.ddl

import groovy.transform.ToString

/**
 * @author IWEERARATHNA
 */
@ToString
class DTable extends DAbstractEntity {

    List<DField> fields = []
    List<DKey> keys = []

    boolean temporary = false
    boolean ifNotExist = true

    DTable FIELD(String name, DFieldType type, Map details=null) {
        DField dField = DField.parseFrom(details)
        dField.type = type
        dField.name = name
        fields.add(dField)
        return this
    }

    DTable PRIMARY_KEY(String fieldName, Map details=null) {
        DKey dKey = DKey.parseFrom(details)
        dKey.fields = [fieldName]
        dKey.type = DKeyType.PRIMARY
        keys.add(dKey)
        return this
    }

    DTable PRIMARY_KEY(List<String> fieldNames, Map details=null) {
        DKey dKey = DKey.parseFrom(details)
        dKey.fields = fieldNames
        dKey.type = DKeyType.PRIMARY
        keys.add(dKey)
        return this
    }

    DTable PRIMARY_KEY(String... fieldNames) {
        DKey dKey = new DKey()
        dKey.fields = fieldNames
        dKey.type = DKeyType.PRIMARY
        keys.add(dKey)
        return this
    }

    DTable INDEX(String name, Map details=null) {
        DKey dKey = DKey.parseFrom(details)
        dKey.type = DKeyType.INDEX
        dKey.name = name
        keys.add(dKey)
        return this
    }

    DTable FOREIGN_KEY(String name, String thisTableField, Map details=null) {
        DKey dKey = DKey.parseFrom(details)
        dKey.type = DKeyType.FOREIGN
        dKey.fields = [thisTableField]
        dKey.name = name
        keys.add(dKey)
        return this
    }

    private <T> T invokeIn(T obj, closure) {
        def code = closure.rehydrate(obj, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        return code()
    }

}
