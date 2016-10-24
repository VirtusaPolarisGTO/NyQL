package com.virtusa.gto.insight.nyql.ddl

import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class DKey extends DAbstractEntity {

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

        dKey.name = values['name']
        dKey.comment = values['comment']
        dKey.type = (DKeyType) toEnum(values, 'type', DKeyType.INDEX)
        dKey.unique = (boolean)(values['unique'] ?: false)
        dKey.fields = (List)(values['fields'] ?: [])
        dKey.refTable = values['refTable'] ?: null
        dKey.refFields = (List)(values['refFields'] ?: [])

        dKey.indexType = (DKeyIndexType) toEnum(values, 'indexType', null)
        dKey.onDelete = (DReferenceOption) toEnum(values, 'onDelete', DReferenceOption.NO_ACTION)
        dKey.onUpdate = (DReferenceOption) toEnum(values, 'onUpdate', DReferenceOption.NO_ACTION)
        dKey
    }

}
