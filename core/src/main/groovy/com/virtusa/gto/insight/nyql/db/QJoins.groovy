package com.virtusa.gto.insight.nyql.db

import com.virtusa.gto.insight.nyql.QContextType
import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
trait QJoins {

    String JOIN(QContextType contextType) { ',' }
    String LEFT_JOIN(QContextType contextType) { 'LEFT JOIN' }
    String LEFT_OUTER_JOIN(QContextType contextType) { 'LEFT OUTER JOIN' }
    String RIGHT_JOIN(QContextType contextType) { 'RIGHT JOIN' }
    String RIGHT_OUTER_JOIN(QContextType contextType) { 'RIGHT OUTER JOIN' }
    String INNER_JOIN(QContextType contextType) { 'INNER JOIN' }

}