package com.virtusa.gto.insight.nyql.ddl

import groovy.transform.ToString

/**
 * @author IWEERARATHNA
 */
@ToString
class DField extends DAbstractEntity {

    boolean sequence = false
    boolean notNull = false
    DFieldType type
    long length = -1

    boolean unsigned = false
    boolean specifiedDefault = false
    def defaultValue = null

    static DField parseFrom(Map values) {
        DField dField = new DField()
        if (values == null) {
            return dField
        }

        dField.name = values['name']
        dField.comment = values['comment']
        dField.unsigned = (boolean)(values['unsigned'] ?: false)
        dField.sequence = (boolean)(values['sequence'] ?: false)
        dField.notNull = (boolean)(values['notNull'] ?: false)
        dField.length = (long)(values['length'] ?: -1)
        dField.type = toEnum(values, 'type', DFieldType.TEXT)
        dField.specifiedDefault = values.containsKey('defaultValue')
        dField.defaultValue = values['defaultValue'] ?: null
        return dField
    }

}
