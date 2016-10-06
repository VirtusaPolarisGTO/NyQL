package com.virtusa.gto.insight.nyql.model

import groovy.transform.Immutable

/**
 * @author IWEERARATHNA
 */
@Immutable
class QSource {

    private String id
    private File file
    private boolean doCache = false
    private GroovyCodeSource codeSource
}
