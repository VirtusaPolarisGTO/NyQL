package com.virtusa.gto.nyql.engine.repo

import com.virtusa.gto.nyql.engine.exceptions.NyScriptExecutionException
import com.virtusa.gto.nyql.engine.exceptions.NyScriptParseException
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.exceptions.NyScriptNotFoundException
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QScriptMapper
import com.virtusa.gto.nyql.model.QSession
import com.virtusa.gto.nyql.model.QSource
import org.codehaus.groovy.control.CompilationFailedException

/**
 * A repository accepting a single file only.
 *
 * @author IWEERARATHNA
 */
class QSingleScriptRepository extends QRepositoryImpl {

    QSingleScriptRepository(QScriptMapper scriptMapper) {
        super(scriptMapper)
    }

    @Override
    QScript parse(String scriptId, QSession session) throws NyException {
        QSource qSource = mapper.map(scriptId)
        if (qSource.codeSource == null) {
            throw new NyScriptNotFoundException(scriptId)
        }

        try {
            Binding binding = new Binding(session?.sessionVariables ?: [:])
            GroovyShell shell = new GroovyShell(binding, caching.makeCompilerConfigs())
            Object result = shell.evaluate(qSource.codeSource)
            QScript script = convertResult(scriptId, result, session)
            return script

        } catch (CompilationFailedException ex) {
            throw new NyScriptParseException(scriptId, src.file, ex)
        } catch (IOException ex) {
            throw new NyScriptExecutionException(scriptId, src.file, ex)
        }
    }
}
