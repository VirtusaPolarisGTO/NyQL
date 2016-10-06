package com.virtusa.gto.insight.nyql.db

import com.virtusa.gto.insight.nyql.QContextType

/**
 * @author IWEERARATHNA
 */
trait QJoins {

    String JOIN(QContextType contextType) { "," }
    String LEFT_JOIN(QContextType contextType) { "LEFT JOIN" }
    String LEFT_OUTER_JOIN(QContextType contextType) { "LEFT OUTER JOIN" }
    String RIGHT_JOIN(QContextType contextType) { "RIGHT JOIN" }
    String RIGHT_OUTER_JOIN(QContextType contextType) { "RIGHT OUTER JOIN" }
    String FULL_OUTER_JOIN(QContextType contextType) { "FULL OUTER JOIN" }
    String INNER_JOIN(QContextType contextType) { "INNER JOIN" }

    String OP_EQUAL() { "=" }
    String OP_IN() { "IN" }
    String OP_NOTIN() { "NOT IN" }

}