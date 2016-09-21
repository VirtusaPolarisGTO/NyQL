package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.QScript

/**
 * @author IWEERARATHNA
 */
trait QExecutor {

    abstract void startTransaction() throws NyException

    abstract void commit() throws NyException

    abstract def checkPoint() throws NyException

    abstract void rollback(def checkpoint) throws NyException

    abstract void done() throws NyException

    abstract def execute(QScript script) throws Exception

}
