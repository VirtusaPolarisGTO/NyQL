package com.virtusa.gto.insight.nyql.model

import com.virtusa.gto.insight.nyql.DSL

/**
 * The base script class for every nyql script.
 *
 * @author IWEERARATHNA
 */
abstract class NyBaseScript extends Script {

    static final String TEMP_SESSION_VAR = '$_SESSION'

    private QSession session

    protected NyBaseScript() {
        super()
    }

    protected NyBaseScript(Binding binding) {
        super(binding)
    }

    void setSession(QSession session) {
        this.session = session
        $DSL = new DSL(session)
        $SESSION = session.sessionVariables
    }

    /**
     * State of cachable.
     */
    boolean doCache = false

    /**
     * DSL instance being used to create queries and scripts.
     */
    DSL $DSL

    /**
     * Session instance holing variables.
     */
    Map $SESSION
}
