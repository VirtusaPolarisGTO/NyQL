package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.exceptions.NyException

/**
 * A base interface to execute a generated script.
 *
 * @author IWEERARATHNA
 */
trait QExecutor implements Closeable {

    /**
     * Called when a script is ready to start a new transaction.
     *
     * @throws NyException any exception thrown while starting transaction.
     */
    abstract void startTransaction() throws NyException

    /**
     * Called when transaction needed to be committed.
     *
     * @throws NyException any exception thrown while committing.
     */
    abstract void commit() throws NyException

    /**
     * Called when script needs to save a snapshot of in current execution point,
     * so that later it might revert into.
     *
     * @return a reference to the checkpoint.
     * @throws NyException any exception thrown while creating a checkpoint.
     */
    abstract def checkPoint() throws NyException

    /**
     * Called when script is rollback the transaction or to a pre-saved checkpoint.
     *
     * @param checkpoint pre-saved checkpoint. If this is null, then whole transaction will be rolled-back.
     * @throws NyException any exception thrown while roll backing.
     */
    abstract void rollback(def checkpoint) throws NyException

    /**
     * Done is called when transaction is completed successfully or with a failure.
     *
     * @throws NyException any exception thrown while closing transaction.
     */
    abstract void done() throws NyException

    /**
     * Executes a sequence of scripts in one go.
     *
     * @param scriptList list of scripts to run.
     * @return accumulated result set of all scripts ran.
     * @throws Exception any exception thrown while executing.
     */
    def execute(QScriptList scriptList) throws Exception {
        if (scriptList == null || scriptList.scripts == null) {
            return null;
        }

        List results = []
        for (QScript qScript : scriptList.scripts) {
            def res = execute(qScript)
            results.add(res)
        }
        return results
    }

    /**
     * Executes the given script and returns the result of that execution.
     *
     * @param script script to be executed.
     * @return result of the execution.
     * @throws Exception any exception thrown while executing.
     */
    abstract def execute(QScript script) throws Exception

}
