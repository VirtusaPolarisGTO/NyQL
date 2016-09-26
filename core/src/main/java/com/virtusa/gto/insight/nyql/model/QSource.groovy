package com.virtusa.gto.insight.nyql.model

import groovy.transform.Immutable

/**
 * @author IWEERARATHNA
 */
@Immutable
class QSource {

    private final String id
    private final File file
    private boolean doCache = false

}
