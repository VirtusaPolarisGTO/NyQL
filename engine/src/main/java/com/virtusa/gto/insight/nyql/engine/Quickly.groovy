package com.virtusa.gto.insight.nyql.engine

import com.virtusa.gto.insight.nyql.model.QExecutor
import com.virtusa.gto.insight.nyql.configs.Configurations
import com.virtusa.gto.insight.nyql.model.QExecutorRegistry
import com.virtusa.gto.insight.nyql.model.QRepositoryRegistry
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QScriptMapper
import com.virtusa.gto.insight.nyql.model.QSession
import com.virtusa.gto.insight.nyql.model.QSource
import com.virtusa.gto.insight.nyql.engine.impl.QDummyExecutor
import com.virtusa.gto.insight.nyql.engine.impl.QJdbcExecutor
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.Connection
import java.sql.DriverManager

/**
 * @author IWEERARATHNA
 */
final class Quickly {

    private static final Logger LOGGER = LoggerFactory.getLogger(Quickly.class)

    static void configOnce() {
        configOnce((Properties)null)
    }

    static void configOnce(File configJsonFile) {
        configOnce(new JsonSlurper().parse(configJsonFile) as Map)
    }

    static void configOnce(Map properties) {
        if (properties == null) {
            InputStream inputStream = null
            try {
                inputStream = Quickly.class.classLoader.getResourceAsStream("nyql.json")
                Map config = new JsonSlurper().parse(inputStream) as Map
                Configurations.instance().configure(config)
            } finally {
                if (inputStream) {
                    inputStream.close()
                }
            }

        } else {
            Configurations.instance().configure(properties)
        }

        QExecutor executor = new QDummyExecutor();
        QExecutorRegistry.getInstance().register("default", executor);
    }

    static QScript parse(Map sessionVariables=null) {
        QSession qSession = QSession.create()
        if (sessionVariables) {
            qSession.getSessionVariables().putAll(sessionVariables)
        }
        QRepositoryRegistry.instance.defaultRepository().parse("any", qSession)
    }

    static QScript parse(String name, Map sessionVariables=null) {
        QSession qSession = QSession.create()
        if (sessionVariables) {
            qSession.sessionVariables.putAll(sessionVariables)
        }
        QRepositoryRegistry.instance.defaultRepository().parse(name, qSession)
    }

    static def execute(String name, Map sessionVariables=null, QExecutor executor=null) throws Exception {
        Connection connection = null
        try {
            connection = create()
            if (executor == null) {
                executor = new QJdbcExecutor(connection);
                QExecutorRegistry.getInstance().register("jdbc", executor);
            }

            QScript script = parse(name, sessionVariables)

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

    static def execute(Map sessionVariables=null) throws Exception {
        Connection connection = null
        try {
            connection = create()
            QJdbcExecutor executor = new QJdbcExecutor(connection);
            QExecutorRegistry.getInstance().register("jdbc", executor);

            QScript script = parse(sessionVariables)

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

    private static Connection create() throws Exception {
        InputStream inputStream = null
        try {
            inputStream = Quickly.class.classLoader.getResourceAsStream("jdbc.properties")
            Properties config = new Properties()
            config.load(inputStream)

            // load jdbc
            Class.forName(config.getProperty("driver"));
            return DriverManager.getConnection(config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password"));

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
