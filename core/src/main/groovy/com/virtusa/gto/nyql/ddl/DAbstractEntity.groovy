package com.virtusa.gto.nyql.ddl

import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
abstract class DAbstractEntity {

    String name
    String comment

    Map additionalAttrs = [:]

    static <T> T toEnum(Map map, String key, T defValue) {
        if (map.containsKey(key)) {
            (T) map[key]
        } else {
            defValue
        }
    }

}
