package com.virtusa.gto.insight.nyql.ddl

import groovy.transform.PackageScope

/**
 * @author IWEERARATHNA
 */
@PackageScope abstract class DAbstractEntity {

    String name
    String comment

    Map additionalAttrs = [:]

    public static <T> T toEnum(Map map, String key, T defValue) {
        if (map.containsKey(key)) {
            return (T) map[key]
        } else {
            return defValue
        }
    }

}
