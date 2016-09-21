package com.virtusa.gto.insight.nyql.ddl

import groovy.transform.PackageScope

import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * @author IWEERARATHNA
 */
@PackageScope class DKey extends DAbstractEntity {

    DKeyType type
    List<String> fields
    String refTable
    List<String> refFields
    boolean unique
    DKeyIndexType indexType
    DReferenceOption onUpdate = DReferenceOption.NO_ACTION
    DReferenceOption onDelete = DReferenceOption.NO_ACTION

    static DKey parseFrom(Map values) {
        DKey dKey = new DKey()
        if (values == null) {
            return dKey
        }

        dKey.name = values["name"]
        dKey.comment = values["comment"]
        dKey.type = toEnum(values, "type", DKeyType.INDEX)
        dKey.unique = (boolean)(values["unique"] ?: false)
        dKey.fields = Stream.of((values["fields"] ?: "").toString().split("[,]")).collect(Collectors.toList())
        dKey.refTable = values["refTable"] ?: null
        dKey.refFields = Stream.of((values["refFields"] ?: "").toString().split("[,]")).collect(Collectors.toList())

        dKey.indexType = toEnum(values, "indexType", null)
        dKey.onDelete = toEnum(values, "onDelete", DReferenceOption.NO_ACTION)
        dKey.onUpdate = toEnum(values, "onUpdate", DReferenceOption.NO_ACTION)
        return dKey
    }

}
