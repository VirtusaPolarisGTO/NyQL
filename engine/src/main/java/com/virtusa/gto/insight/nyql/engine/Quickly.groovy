package com.virtusa.gto.insight.nyql.engine

import com.virtusa.gto.insight.nyql.DSLContext
import com.virtusa.gto.insight.nyql.QExecutor
import com.virtusa.gto.insight.nyql.model.QExecutorRegistry
import com.virtusa.gto.insight.nyql.model.QRepository
import com.virtusa.gto.insight.nyql.model.QRepositoryRegistry
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QScriptMapper
import com.virtusa.gto.insight.nyql.model.QSession
import com.virtusa.gto.insight.nyql.model.QSource
import com.virtusa.gto.insight.nyql.engine.impl.QDummyExecutor
import com.virtusa.gto.insight.nyql.engine.impl.QJdbcExecutor
import com.virtusa.gto.insight.nyql.engine.repo.QRepositoryImpl
import com.virtusa.gto.insight.nyql.engine.repo.QScriptsFolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.Connection
import java.sql.DriverManager

/**
 * @author IWEERARATHNA
 */
final class Quickly {

    private static final Logger LOGGER = LoggerFactory.getLogger(Quickly.class)

    static void configOnce(Properties properties) {
        if (properties == null) {
            InputStream inputStream = null
            try {
                inputStream = Quickly.class.classLoader.getResourceAsStream("nyql.properties")
                Properties config = new Properties()
                config.load(inputStream)
                DSLContext.load(config)
            } finally {
                if (inputStream) {
                    inputStream.close()
                }
            }

        } else {
            DSLContext.load(properties)
        }

        QExecutor executor = new QDummyExecutor();
        QExecutorRegistry.getInstance().register("default", executor);
    }

    static QScript parse(File file, Map sessionVariables=null) {
        QScriptMapper scriptFile = new QScriptFile(oneFile: file)
        QRepository repository = new QRepositoryImpl(scriptFile)
        QRepositoryRegistry.getInstance().register("default", repository);

        QSession qSession = QSession.create()
        if (sessionVariables) {
            qSession.getSessionVariables().putAll(sessionVariables)
        }
        repository.parse("any", qSession)
    }

    static QScript parse(File dir, String name, Map sessionVariables=null) {
        QScriptMapper scriptFile = new QScriptsFolder(dir)
        QRepository repository = new QRepositoryImpl(scriptFile)
        QRepositoryRegistry.getInstance().register("default", repository);

        QSession qSession = QSession.create()
        if (sessionVariables) {
            qSession.sessionVariables.putAll(sessionVariables)
        }
        repository.parse(name, qSession)
    }

    static def execute(File dir, String name, Map sessionVariables=null, QExecutor executor=null) throws Exception {
        Connection connection = null
        try {
            connection = create()
            if (executor == null) {
                executor = new QJdbcExecutor(connection);
                QExecutorRegistry.getInstance().register("jdbc", executor);
            }

            QScript script = parse(dir, name, sessionVariables)

            long s = System.currentTimeMillis()
            def result = executor.execute(script);
            LOGGER.debug("Query execution took time: {} ms", (System.currentTimeMillis() - s))
            return result

        } finally {
            if (connection != null) {
                connection.close()
            }
        }
    }

    static def execute(File file, Map sessionVariables=null) throws Exception {
        Connection connection = null
        try {
            connection = create()
            QJdbcExecutor executor = new QJdbcExecutor(connection);
            QExecutorRegistry.getInstance().register("jdbc", executor);

            QScript script = parse(file, sessionVariables)

            long s = System.currentTimeMillis()
            def result = (List<Map<String,Object>>) executor.execute(script);
            LOGGER.debug("Query execution took time: {} ms", (System.currentTimeMillis() - s))
            return result

        } finally {
            if (connection != null) {
                connection.close()
            }
        }
    }

    private static Connection create() throws Exception {
        InputStream inputStream = null
        try {
            inputStream = Quickly.class.classLoader.getResourceAsStream("jdbc.properties")
            Properties config = new Properties()
            config.load(inputStream)

            // load jdbc
            Class.forName(config.getProperty("jdbc.driver"));
            return DriverManager.getConnection(config.getProperty("jdbc.url"),
                    config.getProperty("jdbc.username"),
                    config.getProperty("jdbc.password"));

        } finally {
            if (inputStream) {
                inputStream.close()
            }
        }


    }

    private static class QScriptFile implements QScriptMapper {

        File oneFile
        QSource src = null

        @Override
        QSource map(String id) {
            return loadIf()
        }

        private QSource loadIf() {
            if (src != null) {
                return src
            }
            src = new QSource(id: oneFile.name, file: oneFile, doCache: true)
            return src
        }

        @Override
        Collection<QSource> allSources() {
            loadIf()
            List<QSource> list = new LinkedList<QSource>()
            list.add(src)
            return list
        }
    }
}
