package com.virtusa.gto.insight.nyql.model.impl

import com.virtusa.gto.insight.nyql.configs.Configurations
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.QRepository
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QScriptResult
import com.virtusa.gto.insight.nyql.model.QSession

/**
 * @author IWEERARATHNA
 */
class QProfRepository implements QRepository {

    private final QRepository repository

    public QProfRepository(QRepository theRepository) {
        repository = theRepository
    }

    @Override
    void clearCache(int level) {
        repository.clearCache(level)
    }

    @Override
    QScript parse(String scriptId, QSession session) throws NyException {
        long s = System.currentTimeMillis()
        def result = repository.parse(scriptId, session)
        long e = System.currentTimeMillis()

        if (result instanceof QScriptResult) {
            Configurations.instance().getProfiler().doneExecuting(result, (e - s))
        } else {
            Configurations.instance().getProfiler().doneParsing(scriptId, (e - s), session)
        }
        return result
    }

    @Override
    void close() throws IOException {
        repository.close()
    }
}
